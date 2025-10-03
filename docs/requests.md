# Making HTTP Requests to Badger

Badger is an HTTP server. Based on configuration, it will serve various endpoints. When
you're developing an application on top of Badger, this page is the most relevant to
you.

Badger's server endpoints are defined in the [router file](./router.md). The server simply
serves exactly what is configured there.
As of Badger version `1.0-beta.7`, the router can define four types of endpoints:
template endpoints, batch endpoints, bulk endpoints and raw endpoints.

Template endpoints generate a single image from a [template file](./templates.md).
Batch endpoints generate multiple templates, as specified by a [batch file](./batches.md).
Bulk endpoints generate multiple batches, as specified by a [bulk file](./batches.md) and the request body.
Raw endpoints serve an asset without any modification.
Template and batch endpoint types depend on query parameters for input. Bulk endpoints may
need query parameters, but their main source of input is the request body. The exact query parameters
recognised by each endpoint are specified in the router file. Raw endpoints never take any inputs,
they just serve files.

## The Root Endpoint

The root endpoint, `/`, is a special endpoint that just serves as a ping endpoint. This
endpoint will simply return `200 OK` with some basic server information. Badger will
always serve this endpoint, even without a router file or any configuration. It serves
as a ping endpoint to reassure that the Badger server is running.

## Other Endpoints

### Template and Batch Endpoints

Template endpoints and batch endpoints can be called through a GET
request. You provide the required parameters as query parameters. The router specifies
which query parameters are required, and which are optional. See [Getting Help](#getting-help)
to learn more about how to learn which parameters are required with each endpoint.

### Raw Endpoints

Raw endpoints just serve raw, unprocessed content. They are accessed through a GET
request and don't take any query parameters.

### Bulk Endpoints

Bulk endpoints serve multiple batches in bulk. Unlike other endpoints, you access these
through a POST request. They can ask for query parameters, but they mainly depend on
the request body.

Unlike batches, bulk endpoints don't have a predefined list of entries they export, but
instead they export the entries given in the request body. This body is in JSON format,
which is an array of objects. Each object must specify a batch type, and per batch type
there are some parameters that you may need to specify. An example body may look like this:

```json
[
  {
    "type": "batch1",
    "params": { "color": "#FF0" }
  },
  {
    "type": "batch2",
    "params": { "color": "#F0F", "text": "Hello world!" }
  },
  {
    "type": "batch2",
    "params": { "color": "#000", "text": "Foo!" }
  }
]
```

## Getting Help

The router file specifies all the endpoints of the server. Since this file may not
be accessible to you as an application developer, Badger can serve you information about
the available routes and their required parameters using a help-flag.

To get help with
a specific endpoint, simply make a GET request to that endpoint with
`-help` in the query parameters, e.g. `/endpoint?-help`.
The `-` in front of the name indicates that this is a query parameter built into
the Badger server (a system parameter), not a query parameter specified by the router.
Other system parameters exist.

With the `-help` flag, the server will output what the endpoint does and what
query parameters it accepts, instead of calling the endpoint.
An example help output may be:

```
-- Badger --
# An example endpoint, these descriptions can all be set in the router file
/endpoint:  (template)
 - parameter: any (required)    # An example query parameter
 - optional: int (optional, default: 0)    # An example optional parameter with type
```

When you give the `-help` query parameter to the root endpoint, i.e. `/?-help`, it will
simply print the help of every endpoint.

## Output Formats

Badger can output generated templates and badges in various formats. Templates generate
into a single image, batches into a collection of multiple images. With the `-format`
query parameter, you may specify what output format Badger uses when exporting templates
and batches.

### Template Output Formats

| Format          | MIME Type         | Description                                        |
|-----------------|-------------------|----------------------------------------------------|
| `png` (default) | `image/png`       | A PNG file                                         |
| `jpg`, `jpeg`   | `image/jpeg`      | A JPEG file                                        |
| `webp`          | `image/webp`      | A WEBP file                                        |
| `pdf`           | `application/pdf` | A PDF file with a single page containing the image |

### Batch Output Formats

| Format              | MIME Type         | Description                                                                                                  |
|---------------------|-------------------|--------------------------------------------------------------------------------------------------------------|
| `hugepdf` (default) | `application/pdf` | A PDF file with each batch entry on a single page, in the order defined in the batch file                    |
| `pngzip`            | `application/zip` | A ZIP archive with each entry in a distinct PNG file, named by the entry names in the batch file             |
| `jpgzip`, `jpegzip` | `application/zip` | A ZIP archive with each entry in a distinct JPEG file, named by the entry names in the batch file            |
| `webpzip`           | `application/zip` | A ZIP archive with each entry in a distinct WEBP file, named by the entry names in the batch file            |
| `pdfzip`            | `application/zip` | A ZIP archive with each entry in a distinct single-page PDF file, named by the entry names in the batch file |

### Bulk Output Formats

| Format                 | MIME Type         | Description                                                                                                                                     |
|------------------------|-------------------|-------------------------------------------------------------------------------------------------------------------------------------------------|
| `extremepdf` (default) | `application/pdf` | A PDF file with each batch concatenated into one long PDF                                                                                       |
| `hugepdfzip`           | `application/zip` | A ZIP archive with each batch in a separate multi-page PDF                                                                                      |
| `pngzip`               | `application/zip` | A ZIP archive with each batch entry in a distinct PNG file, named by the entry names in the batch file, grouped by bulk entry index             |
| `jpgzip`, `jpegzip`    | `application/zip` | A ZIP archive with each batch entry in a distinct JPEG file, named by the entry names in the batch file, grouped by bulk entry index            |
| `webpzip`              | `application/zip` | A ZIP archive with each batch entry in a distinct WEBP file, named by the entry names in the batch file, grouped by bulk entry index            |
| `pdfzip`               | `application/zip` | A ZIP archive with each batch entry in a distinct single-page PDF file, named by the entry names in the batch file, grouped by bulk entry index |

## Errors

### 400 Bad Request

When the required query parameters are not specified on an endpoint, Badger will return
a `400 Bad Request` status code, usually with a message of what's wrong and the help
text of the accessed endpoint:

```
-- Badger --
400 Bad Request
Missing required input parameter 'parameter'

# An example endpoint, these descriptions can all be set in the router file
/endpoint:  (template)
 - parameter: any (required)    # An example query parameter
 - optional: int (optional, default: 0)    # An example optional parameter with type
```

### 404 Not Found

When accessing an endpoint that was not defined in the router file (and that isn't `/`),
Badger will return a `404 Not Found` status code. Usually, it will append a compact list
of the endpoints served by Badger:

```
-- Badger --
404 Not Found

Available routes:
 - /endpoint
 - /other/endpoint
```

### 405 Method Not Allowed

When accessing an endpoint with the wrong method, Badger will return a
`405 Method Not Allowed` status code.

```
-- Badger --
405 Method Not Allowed
Use POST on bulk endpoints
```

### 500 Internal Server Error

Currently, some of the internal evaluation of Badger can cause a wrong input to generate
a `500 Internal Server Error`, despite it being a client issue.
However, this error is generated whenever an exception
is thrown in Badger, and may indicate a bug. In most cases, Badger will simply catch
the exception and return a text message with the stack trace.

Badger attempts to return the stack trace with the 500 status code, but it can happen
that it fails to do so and may simply return no content with the error. This is always
a bug and should be reported in an [issue](https://github.com/FoxSamu/badger/issues).


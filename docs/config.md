# Configuring Badger

Badger is highly configurable. Nearly all configuration is provided to Badger in the form of _assets_.
Assets are small files located in the assets directory, or fetched remotely. Most configuration
is done with YAML files, so a basic understanding of the YAML format is important (luckily,
YAML isn't hard to understand).

The remaining bit of configuration that is not done via assets is mainly to tell Badger where
it could find the assets. We call this bit the _primary configuration_.

The primary configuration can be loaded from 3 places:

- a config file;
- JVM properties; or
- environment variables.

For every configuration option, badger first tries getting it from the environment variables,
then from the JVM properties, and at last from the config file. Every property has a default
value, so if no configuration is specified, Badger will still start, though it will log a thing
or two about it.

The config file is by default loaded from `./badger.yml`. This location can be overridden with
the `CONFIG_PATH` environment variable, or the `net.foxboi.badger.config_path` JVM property.
The file doesn't need to be present, even when the path to it is explicitly specified. When
the file is absent, it is simply loads the default values.

# Config Properties

The following configuration options are available. We'll describe them below, but first we'll give a quick overview.

| Env. Variable          | JVM Property                             | Config File                     | Default                                           |
|------------------------|------------------------------------------|---------------------------------|---------------------------------------------------|
| `ASSET_SOURCE`         | `net.foxboi.badger.asset_source`         | `asset_source` (see note below) | `empty`                                           |
| `ASSETS_DIR`           | `net.foxboi.badger.assets_dir`           | `asset_source > directory`      | `null`                                            |
| `ASSET_ENDPOINT`       | `net.foxboi.badger.asset_endpoint`       | `asset_source > endpoint`       | `null`                                            |
| `ASSET_BUCKET`         | `net.foxboi.badger.asset_bucket`         | `asset_source > bucket`         | `null`                                            |
| `MINIO_ACCESS_KEY`     | `net.foxboi.badger.minio_access_key`     | `asset_source > access_key`     | `null`                                            |
| `MINIO_SECRET_KEY`     | `net.foxboi.badger.minio_secret_key`     | `asset_source > secret_key`     | `null`                                            |
| `MINIO_NO_CREDENTIALS` | `net.foxboi.badger.minio_no_credentials` | `asset_source > no_credentials` | `false`                                           |
| `ASSET_PREFIX`         | `net.foxboi.badger.asset_prefix`         | `asset_source > asset_prefix`   | `""`                                              |
| `TEMP_DIR`             | `net.foxboi.badger.temp_dir`             | `temp_dir`                      | System temporary directory (e.g. `/tmp` on Linux) |
| `ROUTER`               | `net.foxboi.badger.router`               | `router`                        | `null`                                            |

Note that while the mapping from environment variables to JVM properties is linear, the mapping to config file
entries is slightly different as it uses the tree-like structure of YAML. Specifically, the `asset_source` key
specifies where and how to find assets (described in more detail below), which comes with a few extra properties that
depend on the type of asset
source. In the YAML format, this is configured as subproperties of a _tagged object_. So a `local` asset source
is configured like this (we put the environment variables next to the YAML code for comparison):

```yaml
asset_source: !<local>         # ASSET_SOURCE = local
  directory: ./assets          # ASSETS_DIR = ./assets
```

The most important options to specify are `asset_source`, any properties related to the
chosen asset source (see below), and `router`. A basic configuration could be:

```yaml
asset_source: !<local>         # ASSET_SOURCE = local
  directory: ./assets          # ASSETS_DIR = ./assets

router: asset://router.yml     # ROUTER = asset://router.yml
```

## Asset Sources

As said above, assets are the backbone of Badger, and without assets, Badger can't do anything.
Therefore it is crucial to declare where assets are located. Badger loads assets via an _asset source_,
which is an interface that simply allows two operations: checking if an asset exists, and reading an
asset. This abstraction allows for assets to be fetched from remote sources, such as an S3 bucket.
As of version `1.0-beta.2`, Badger supports 3 types of asset sources: `empty`, `local` and `minio`.
More about assets is described on the [Assets](./assets.md) page.

### The `empty` Source

The `empty` asset source specifies no assets. This is usually unintended, but it's the default
value and can be useful when all assets are remotely configured. The empty asset source
takes no extra properties.

The `empty` source is also the asset source that Badger falls back to when crucial properties
of the configured asset source are not defined.

```yaml
asset_source: !<empty>         # ASSET_SOURCE = empty
```

### The `local` Source

The `local` asset source specifies that assets are loaded from directory on the local
file system. The exact directory is given by the `directory` subproperty, which specifies
the root directory of _all_ assets. When the `directory` subproperty is not defined, then
Badger doesn't know where to find the local assets, and it will fall back to an `empty`
source.

```yaml
asset_source: !<local>         # ASSET_SOURCE = local
  directory: ./assets          # ASSETS_DIR = ./assets
```

### The `minio` Source

The `minio` asset source specifies that assets are loaded from an S3 bucket, using
a [MinIO](https://www.min.io/) client. The client is configured using the following
options:

- `endpoint` specifies the endpoint URL to access the S3 storage;
- `bucket` specifies the S3 bucket name where the objects are stored;
- `asset_prefix` specifies a prefix that must be prepended to every asset path;
- `access_key` specifies an access key to the S3 server;
- `secret_key` specifies a secret key to the S3 server;
- `no_credentials` specifies that credentials are to be ignored.

Badger will attempt to access an S3 endpoint without credentials when `access_key`
and `secret_key` are unset, but it will log a warning. This warning is disabled when
`no_credentials` is explicitly set to `true`. Note that setting `no_credentials` to
`true` will make Badger ignore `access_key` and `secret_key`, even when they are
specified.

The `asset_prefix` option specifies a raw prefix, so when this is, say, `assets/`, then
every asset path is prefixed with `assets/` to obtain the object name. Note that this is
not a directory name but a _raw prefix_. Thus, when the prefix is `foo`, then an asset path
like `bar/baz` maps to the object name `foobar/baz`, not `foo/bar/baz`.

```yaml
asset_source: !<minio>                   # ASSET_SOURCE = minio
  endpoint: https://play.min.io          # ASSET_ENDPOINT = https://play.min.io
  bucket: badger_assets                  # ASSET_BUCKET = badger_assets
  asset_prefix: assets/                  # ASSET_PREFIX = assets/
  access_key: ...                        # MINIO_ACCESS_KEY = ...
  secret_key: ...                        # MINIO_SECRET_KEY = ...
  no_credentials: false                  # MINIO_NO_CREDENTIALS = false
```

## Temporary Directory

Badger sometimes needs to write temporary files, such as cached downloads and generated
PDF files. These files are written to a special temporary directory, which by default is
the system's default temporary directory. The temporary directory may be changed in the
configuration file, using the `temp_dir` property:

```yaml
temp_dir: ./tmp                 # TEMP_DIR = ./tmp
```

## Router

The _router_ is a special asset that declares which routes are available via the
Badger HTTP server. The router is extensively discussed on the [Router](./router.md) page.
The configuration file specifies the URI of the router asset, which is then loaded
from the configured asset source. The router is specified with the `router` property:

```yaml
router: asset://router.yml      # ROUTER = asset://router.yml
```

Note that when no router is specified, then the server will only serve the `/` endpoint,
all other endpoints will return a 404 status code.

# Which Configuration Method Do I Use?

While possible to use, it is discouraged to use the JVM properties. It is almost always
preferable to use the environment variables or the configuration file. Note that you
can use both at the same time: the config file defines the base configuration, and the
environment variables define runtime-dependent configuration. The best way is to supply
sensitive configuration values (such as the MinIO secret key) via environment
variables, while writing other information in a config file. However, it is perfectly
fine to have no config file and simply use the environment variables.

## A Note on Overriding Properties

When setting the
asset source, any properties that are not related to the configured asset source will be
completely forgotten. To demonstrate this, consider the following config file:

```yaml
asset_source: !<minio>
  # Options for `minio` asset source
  endpoint: https://play.min.io
  bucket: badger_assets
  asset_prefix: assets/
  access_key: ...
  secret_key: ...

  # Options for `local` asset source
  directory: ./assets
```

Now, if you set the environment variable `ASSET_SOURCE` to `local`, Badger will tell you
that the assets directory was not specified, despite it being in the config file. This is
due to how the config file is parsed: it sees that the asset source is a `minio` asset source,
so it will parse all the related properties, but the `directory` property is skipped and
forgotten about the moment it starts loading environment variables. Thus, when the environment
variables specify that the asset source is a `local` asset source instead, it no longer knows
what value was set for the `directory` property.

This can be confusing, so to avoid confusion, either use no config file and specify all
configuration as environment variables, or specify only the secret options in the envonment
variables. You can override subproperties of the asset source just fine, so considering
the above config file, you can set `MINIO_SECRET_KEY` and it will override the `secret_key`
property while keeping the others the same. In case your config file is kept secret, you may also put the secrets in the
config file itself.


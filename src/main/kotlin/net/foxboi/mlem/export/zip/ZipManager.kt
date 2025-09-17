package net.foxboi.mlem.export.zip

import kotlinx.io.files.Path
import net.foxboi.mlem.Mlem

class ZipManager(
    val tmpDir: Path
) {
    fun getBuilder(): ZipBuilder {
        val uuid = Mlem.uuid()
        val path = Path(tmpDir, "zip_$uuid.zip")
        return ZipBuilder(path)
    }
}
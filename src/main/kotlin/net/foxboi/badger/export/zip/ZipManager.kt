package net.foxboi.badger.export.zip

import kotlinx.io.files.Path
import net.foxboi.badger.Badger

class ZipManager(
    val tmpDir: Path
) {
    fun getBuilder(): ZipBuilder {
        val uuid = Badger.uuid()
        val path = Path(tmpDir, "zip_$uuid.zip")
        return ZipBuilder(path)
    }
}
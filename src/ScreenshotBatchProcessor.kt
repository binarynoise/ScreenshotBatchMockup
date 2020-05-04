import com.sksamuel.scrimage.ImmutableImage
import com.sksamuel.scrimage.nio.PngWriter
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.nio.file.FileVisitResult
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.SimpleFileVisitor
import java.nio.file.attribute.BasicFileAttributes
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

fun main() {
	val folder = File("E:\\Downloads\\Roh") // TODO replace with actual path
	val target = File("E:\\Downloads\\Mockup")
	val mockupBg = File("mockup+bg.png")
	val mockupNoBg = File("mockup-bg.png")
	
	assert(folder.exists())
	assert(target.exists())
	assert(mockupBg.exists())
	assert(mockupNoBg.exists())
	
	with(mockupBg, mockupNoBg) { mockup ->
		val targetImage = ImmutableImage.loader().fromFile(mockup)
		folder.listFiles { file -> file.extension == "png" }?.forEach { input ->
			
			val relative = input.nameWithoutExtension + "-" + mockup.nameWithoutExtension + ".png"
			println(relative)
			
			val output = target.resolve(relative)
			
			val sourceImage = ImmutableImage.loader().fromFile(input)
			
			val offsetX = 260
			val offsetY = 480
			
			for (x in 0 until 1080) {
				for (y in 0 until 1920) {
					targetImage.setColor(x + offsetX, y + offsetY, sourceImage.pixel(x, y).toColor())
				}
			}
			
			targetImage.output(PngWriter(), output)
		}
	}
	
	compress(target)
}

fun <T> with(vararg items: T, block: (T) -> Unit) {
	for (t in items) t.apply(block)
}

fun compress(dir: File) {
	val sourcePath: Path = dir.toPath()
	val zipFileName = dir.parentFile.resolve("${dir.nameWithoutExtension}.zip")
	
	ZipOutputStream(FileOutputStream(zipFileName)).use { outputStream ->
		Files.walkFileTree(sourcePath, object : SimpleFileVisitor<Path>() {
			override fun visitFile(file: Path, attributes: BasicFileAttributes): FileVisitResult {
				try {
					outputStream.putNextEntry(ZipEntry(sourcePath.relativize(file).toString()))
					Files.copy(file, outputStream)
					outputStream.closeEntry()
				} catch (e: IOException) {
					e.printStackTrace()
				}
				return FileVisitResult.CONTINUE
			}
		})
	}
}

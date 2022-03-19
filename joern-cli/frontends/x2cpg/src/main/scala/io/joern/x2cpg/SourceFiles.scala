package io.joern.x2cpg

import better.files._

object SourceFiles {

  /** For a given set of input paths, determine all source files by inspecting filename extensions.
    */
  def determine(inputPaths: Set[String], extensions: Set[String]): List[String] = {
    def hasSourceFileExtension(file: File): Boolean =
      file.extension.exists(extensions.contains)

    val (dirs, files) = inputPaths
      .map(File(_))
      .partition(_.isDirectory)

    val matchingFiles = files.filter(hasSourceFileExtension).map(_.toString)
    val matchingFilesFromDirs = dirs
      .flatMap(_.listRecursively.filter(hasSourceFileExtension))
      .map(_.toString)

    (matchingFiles ++ matchingFilesFromDirs).toList.sorted
  }

  /** Calculate map that assigns the list of files with extension in `extensions` to each input path in `inputPaths`.
    */
  def expand(inputPaths: Set[String], extensions: Set[String]): Map[String, List[String]] = {
    inputPaths.map { inputPath =>
      inputPath -> determine(Set(inputPath), extensions)
    }.toMap
  }

  /** Copies file into temporary directory and returns file handle for the directory
    */
  def placeFileInTmpDir(fileName: String): File = {
    val sourceFile = File(fileName)
    val dir        = File.newTemporaryDirectory("x2cpg").deleteOnExit()
    sourceFile.copyToDirectory(dir).deleteOnExit()
    dir
  }

}

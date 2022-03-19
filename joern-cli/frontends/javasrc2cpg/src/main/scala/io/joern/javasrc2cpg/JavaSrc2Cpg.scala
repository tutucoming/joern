package io.joern.javasrc2cpg

import better.files.File
import io.joern.javasrc2cpg.passes.AstCreationPass
import io.shiftleft.codepropertygraph.Cpg
import io.shiftleft.codepropertygraph.generated.Languages
import io.joern.x2cpg.passes.frontend.{MetaDataPass, TypeNodePass}
import io.joern.x2cpg.{SourceFiles, X2CpgFrontend}
import io.joern.x2cpg.X2Cpg.withNewEmptyCpg

import scala.jdk.CollectionConverters.EnumerationHasAsScala
import scala.util.Try

object JavaSrc2Cpg {
  val language: String = Languages.JAVASRC

  def apply(): JavaSrc2Cpg = new JavaSrc2Cpg()
}

class JavaSrc2Cpg extends X2CpgFrontend[Config] {

  import JavaSrc2Cpg._
  val sourceFileExtensions = Set(".java")
  private case class DirAndItsFilenames(dir: String, filenames: List[String]) {}

  def createCpg(config: Config): Try[Cpg] = {
    withNewEmptyCpg(config.outputPath, config: Config) { (cpg, config) =>
      new MetaDataPass(cpg, language).createAndApply()
      config.inputPaths.foreach { inputPath =>
        val filenamesForDir = getSourcesFromDir(inputPath)
        val astCreator      = new AstCreationPass(filenamesForDir.dir, filenamesForDir.filenames, cpg)
        astCreator.createAndApply()
        new TypeNodePass(astCreator.global.usedTypes.keys().asScala.toList, cpg)
          .createAndApply()
      }
    }
  }

  /** JavaParser requires that the input path is a directory and not a single source file. This is inconvenient for
    * small-scale testing, so if a single source file is created, copy it to a temp directory.
    */
  private def getSourcesFromDir(sourceCodePath: String): DirAndItsFilenames = {
    val sourceFile = File(sourceCodePath)
    if (sourceFile.isDirectory) {
      val sourceFileNames = SourceFiles.determine(Set(sourceCodePath), sourceFileExtensions)
      DirAndItsFilenames(sourceCodePath, sourceFileNames)
    } else {
      val dir = SourceFiles.placeFileInTmpDir(sourceCodePath)
      DirAndItsFilenames(dir.pathAsString, List(sourceFile.pathAsString))
    }
  }

}

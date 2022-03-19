package io.joern.php2cpg

import io.joern.x2cpg.X2Cpg.withNewEmptyCpg
import io.joern.x2cpg.{SourceFiles, X2CpgConfig, X2CpgFrontend}
import io.joern.x2cpg.passes.frontend.MetaDataPass
import io.shiftleft.codepropertygraph.Cpg
import io.shiftleft.codepropertygraph.generated.Languages
import io.shiftleft.passes.ConcurrentWriterCpgPass

import scala.util.{Failure, Success, Try}

abstract class AstCreationPassBase[T <: X2CpgConfig[_]](cpg: Cpg, sourceFiles : List[String], config : T) extends ConcurrentWriterCpgPass[String](cpg) {

  override def generateParts(): Array[String] = sourceFiles.toArray

  override def runOnPart(diffGraph: DiffGraphBuilder, filename: String): Unit = {
    val localDiff = new DiffGraphBuilder
    createAst(localDiff) match {
      case Success(_) =>
        diffGraph.absorb(localDiff)
      case Failure(exc) =>
        outputException(exc)
    }
  }

  def outputException(throwable: Throwable) : Unit = {
    println(throwable.getMessage)
    throwable.printStackTrace()
  }

  def createAst(diffGraph : DiffGraphBuilder) : Try[Unit]
}

class Php2Cpg extends X2CpgFrontend[Config] {
  override def createCpg(config: Config): Try[Cpg] = {
    withNewEmptyCpg(config.outputPath, config: Config) { (cpg, config) =>
      new MetaDataPass(cpg, Languages.PHP).createAndApply()
      val sourceFiles = SourceFiles.determine(config.inputPaths, Set("php"))

    }
  }
}

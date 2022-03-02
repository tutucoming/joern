package io.joern.javasrc2cpg

import io.joern.x2cpg.{X2Cpg, X2CpgConfig}
import scopt.OParser

/** Command line configuration parameters
  */
final case class Config(inputPaths: List[String] = List.empty, outputPath: String = X2CpgConfig.defaultOutputPath)
    extends X2CpgConfig[Config] {

  override def withAdditionalInputPath(inputPath: String): Config =
    copy(inputPaths = inputPaths.appended(inputPath))
  override def withOutputPath(x: String): Config = copy(outputPath = x)
}

/** Entry point for command line CPG creator
  */
object Main extends App {

  private val frontendSpecificOptions = {
    val builder = OParser.builder[Config]
    import builder.programName
    OParser.sequence(programName("javasrc2cpg"))
  }

  X2Cpg.parseCommandLine(args, frontendSpecificOptions, Config()) match {
    case Some(config) =>
      val cpg = new JavaSrc2Cpg().createCpg(config.inputPaths, Some(config.outputPath))
      cpg.close()
    case None =>
      System.exit(1)
  }

}

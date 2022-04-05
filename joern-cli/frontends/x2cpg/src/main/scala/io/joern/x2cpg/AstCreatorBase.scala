package io.joern.x2cpg

import io.shiftleft.codepropertygraph.generated.nodes.NewNamespaceBlock

import overflowdb.BatchedUpdate.DiffGraphBuilder
import io.joern.x2cpg.AstSubGraphCreator

abstract class AstCreatorBase(filename: String) {
  val diffGraph: DiffGraphBuilder = new DiffGraphBuilder

  def createAst(): DiffGraphBuilder

  def globalNamespaceBlock(): NewNamespaceBlock = AstSubGraphCreator.globalNamespaceBlock(filename)

}

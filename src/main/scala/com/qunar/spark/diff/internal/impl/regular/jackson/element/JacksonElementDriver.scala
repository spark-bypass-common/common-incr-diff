package com.qunar.spark.diff.internal.impl.regular.jackson.element

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node._
import com.qunar.spark.diff.base.ReAssignableArrayBuffer
import com.qunar.spark.diff.base.regular.elements.{CompositeElement, Element, UnitElement}

/**
  * 让Jackson JsonNode适配Element的驱动接口
  *
  * [[JsonNode]]的继承结构如下:
  * * 1 -- JsonNode
  * *   1.1 -- BaseJsonNode
  * *     1.1.1 -- ContainerNode
  * *       1.1.1.1 -- ArrayNode
  * *       1.1.1.2 -- ObjectNode
  * *     1.1.2 -- ValueNode
  * *       1.1.2.1 -- BooleanNode
  * *       1.1.2.2 -- TextNode
  * *       1.1.2.3 -- NumericNode
  * *         1.1.2.3.x -- {IntNode,LongNode,DoubleNode,FloatNode,ShortNode ...}
  *
  * 我们将会以JsonNode的继承结构为基础,实现从[[JsonNode]]到[[com.qunar.spark.diff.base.regular.elements.Element]]的映射.
  */
private[jackson] object JacksonElementDriver {

  /**
    * 用于[[childrenNodesWithName]]存储JsonNode数据
    * NOTICE: 此组件是线程不安全的
    */
  private val jsonNodesBuffer = ReAssignableArrayBuffer[(String, JsonNode)](32)

  /**
    * 列举出给定jsonNode下的所有子JsonNode
    */
  def childrenNodesWithName(jsonNode: JsonNode): Iterable[(String, JsonNode)] = {
    jsonNodesBuffer.reset()
    val jsonNodesEntryIter = jsonNode.fields()

    while (jsonNodesEntryIter.hasNext) {
      val entry = jsonNodesEntryIter.next()
      jsonNodesBuffer += ((entry.getKey, entry.getValue))
    }

    jsonNodesBuffer
  }

  /**
    * 创建一个普通元素
    */
  def makeElement(name: String, jsonNode: JsonNode): Element = {
    jsonNode match {
      case jsonNode: ContainerNode => toCompositeElement(jsonNode, name)
      case jsonNode: ValueNode => toUnitElement(jsonNode, name)
      case _ => throw new IllegalArgumentException(
        "the jsonNode param does not confirm to the correct type BooleanNode which JacksonBooleanElement needs")
    }
  }

  /**
    * 创建一个根元素
    */
  def makeElement(jsonNode: JsonNode): Element = {
    //不指定name,默认是根元素
    val defaultName = "root"
    jsonNode match {
      case jsonNode: ContainerNode => makeElement(defaultName, jsonNode)
      // 如果默认的根元素是ValueNode,则拦截目标Element并将其转为CompositeElement
      case jsonNode: ValueNode =>

      case _ =>
        throw new IllegalArgumentException(
          "the jsonNode param does not confirm to the correct type BooleanNode which JacksonBooleanElement needs")
    }
  }

  /**
    * 转换成CompositeElement
    */
  def toCompositeElement(jsonNode: JsonNode, name: String): CompositeElement = {
    jsonNode match {
      case jsonNode: ObjectNode => JacksonCompositeElement(name)
      case jsonNode: ArrayNode =>
      case _ => throw new IllegalArgumentException(
        "the jsonNode param does not confirm to the correct type BooleanNode which JacksonBooleanElement needs")
    }
  }

  /**
    * 转换成UnitElement
    */
  def toUnitElement(jsonNode: JsonNode, name: String): UnitElement = {
    jsonNode match {
      case jsonNode: TextNode => JacksonTextElement(jsonNode, name)
      case jsonNode: NumericNode => JacksonNumericElement(jsonNode, name)
      case jsonNode: BooleanNode => JacksonBooleanElement(jsonNode, name)
      case _ => throw new IllegalArgumentException(
        "the jsonNode param does not confirm to the correct type BooleanNode which JacksonBooleanElement needs")
    }
  }

}
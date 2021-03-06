package rip.hansolo.script.util

import org.scalajs.dom.Element
import rx.{Rx, Var}

import scala.scalajs.js
import scalatags.JsDom.all._

/**
  * Created by Giymo11 on 09.02.2016.
  */
object ScalatagsRxImplicits {
  object DefaultConverter {
    /**
      * Converts a Rx to a Scalatag and keeps it up to date.
      *
      * @param r The Rx to convert
      * @param converter
      * @tparam T The result-type of the Rx
      * @return The Frag to be used with a Scalatag
      */
    implicit def rxToFrag[T](r: Rx[T])(implicit converter: T => Frag): Frag = {
      def rToFrag = span(r.now).render
      var current = rToFrag

      r triggerLater {
        val newCurrent = rToFrag
        js.Dynamic.global.last = current
        current.parentNode.replaceChild(newCurrent, current)
        current = newCurrent
      }

      current
    }
  }

  object MdlConverter {
    implicit def rxToFrag[T](r: Rx[T])(implicit converter: T => Frag): Frag = {
      val upgrader = js.Dynamic.global.componentUpgrader
      def rToFrag = span(r.now).render
      if(!js.isUndefined(upgrader)) upgrader.upgradeElement(rToFrag)
      var current = rToFrag

      r triggerLater {
        val newCurrent = rToFrag
        js.Dynamic.global.last = current
        current.parentNode.replaceChild(newCurrent, current)
        current = newCurrent
      }

      current
    }
  }


  implicit def VarAttrValue[T: AttrValue] = new AttrValue[Var[T]]{
    def apply(t: Element, a: Attr, r: Var[T]): Unit = {
      r.trigger{ implicitly[AttrValue[T]].apply(t, a, r.now) }
    }
  }
  implicit def RxAttrValue[T: AttrValue] = new AttrValue[Rx[T]]{
    def apply(t: Element, a: Attr, r: Rx[T]): Unit = {
      r.trigger{ implicitly[AttrValue[T]].apply(t, a, r.now) }
    }
  }
  implicit def RxDynamicAttrValue[T: AttrValue] = new AttrValue[Rx.Dynamic[T]]{
    def apply(t: Element, a: Attr, r: Rx.Dynamic[T]): Unit = {
      r.trigger{ implicitly[AttrValue[T]].apply(t, a, r.now) }
    }
  }
}

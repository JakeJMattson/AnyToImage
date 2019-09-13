package io.github.jakejmattson.anytoimage.gui

import javafx.stage.Stage
import tornadofx.App

class AnyToImage : App() {
    override val primaryView = PrimaryView::class
    override fun start(stage: Stage) {
        super.start(stage)
        stage.isResizable = false
    }
}
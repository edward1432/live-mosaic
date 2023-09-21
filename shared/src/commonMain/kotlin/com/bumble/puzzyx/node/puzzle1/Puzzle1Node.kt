package com.bumble.puzzyx.node.puzzle1

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.BottomCenter
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.bumble.appyx.interactions.core.model.transition.Operation.Mode.KEYFRAME
import com.bumble.appyx.navigation.composable.AppyxComponent
import com.bumble.appyx.navigation.modality.BuildContext
import com.bumble.appyx.navigation.node.Node
import com.bumble.appyx.navigation.node.ParentNode
import com.bumble.appyx.navigation.node.node
import com.bumble.puzzyx.component.gridpuzzle.GridPuzzle
import com.bumble.puzzyx.component.gridpuzzle.operation.assemble
import com.bumble.puzzyx.component.gridpuzzle.operation.carousel
import com.bumble.puzzyx.component.gridpuzzle.operation.flip
import com.bumble.puzzyx.component.gridpuzzle.operation.scatter
import com.bumble.puzzyx.composable.EntryCard
import com.bumble.puzzyx.composable.FlashCard
import com.bumble.puzzyx.entries.Entry
import com.bumble.puzzyx.imageloader.toImageBitmap
import com.bumble.puzzyx.puzzle.PuzzlePiece
import com.bumble.puzzyx.ui.colors
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.resource
import kotlin.random.Random

private val animationSpec = spring<Float>(
    stiffness = Spring.StiffnessVeryLow / 15,
    dampingRatio = Spring.DampingRatioNoBouncy
)

class Puzzle1Node(
    buildContext: BuildContext,
    private val imageDirectory: String,
    private val columns: Int,
    private val rows: Int,
    private val gridPuzzle: GridPuzzle = GridPuzzle(
        gridRows = rows,
        gridCols = columns,
        pieces = IntRange(0, rows * columns - 1).map {
            PuzzlePiece(it % columns, it / columns, Entry())
        }.shuffled(),//.take(37), // TODO To test only a subset of elements, uncomment .take
        savedStateMap = buildContext.savedStateMap,
        defaultAnimationSpec = animationSpec
    )
) : ParentNode<PuzzlePiece>(
    buildContext = buildContext,
    appyxComponent = gridPuzzle
) {

    @OptIn(ExperimentalResourceApi::class)
    override fun resolve(puzzlePiece: PuzzlePiece, buildContext: BuildContext): Node =
        node(buildContext) { modifier ->
            val colorIdx = rememberSaveable(puzzlePiece) { Random.nextInt(colors.size) }
            val color = colors[colorIdx]

            Box(
                modifier = modifier
                    .fillMaxWidth(1f / columns)
                    .fillMaxHeight(1f / rows)
            ) {
                FlashCard(
                    flash = Color.White,
                    front = { modifier ->
                        var image: ImageBitmap? by remember { mutableStateOf(null) }
                        LaunchedEffect(Unit) {
                            image =
                                resource("${imageDirectory}slice_${puzzlePiece.j}_${puzzlePiece.i}.png")
                                    .readBytes()
                                    .toImageBitmap()
                        }
                        image?.let {
                            Image(
                                bitmap = it,
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.FillBounds
                            )
                        }
                    },
                    back = { modifier ->
                        EntryCard(
                            modifier = modifier
                                .fillMaxSize()
                                .background(color),
                            puzzlePiece.entry
                        )
                    }
                )
            }
        }

    @Composable
    override fun View(modifier: Modifier) {
        LaunchedEffect(Unit) {
            // We can add the scripted state changes here
            // delay(2500)
            // gridPuzzle.assemble()
            // etc.
        }
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(24.dp),
        ) {
            AppyxComponent(
                appyxComponent = gridPuzzle,
                modifier = Modifier
                    .align(Alignment.Center)
                    .aspectRatio(1f * columns / rows)
                    .background(Color.DarkGray)
            )
            Controls(
                modifier = Modifier.align(BottomCenter)
            )
        }
    }

    @OptIn(ExperimentalLayoutApi::class)
    @Composable
    private fun Controls(modifier: Modifier) {
        FlowRow(
            modifier = modifier,
            horizontalArrangement = Arrangement.Center
        ) {
            Button(onClick = { gridPuzzle.scatter() }) {
                Text("Scatter")
            }
            Button(onClick = { gridPuzzle.assemble(animationSpec = spring(stiffness = 10f)) }) {
                Text("Assemble")
            }
            Button(onClick = { gridPuzzle.flip(KEYFRAME, tween(10000)) }) {
                Text("Flip")
            }
            Button(onClick = { gridPuzzle.carousel() }) {
                Text("Carousel")
            }
        }
    }
}

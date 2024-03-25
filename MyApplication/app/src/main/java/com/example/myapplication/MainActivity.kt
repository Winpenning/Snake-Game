package com.example.myapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.example.myapplication.ui.theme.MyApplicationTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.random.Random

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val game = Game(lifecycleScope)
        setContent {
            MyApplicationTheme {
                // A surface container using the 'background' color from the theme
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    Snake(game)
                }
            }
        }
    }
}
/*
* @author: Paulo Henrique Ziemer dos Santos
* Projeto 1 da matéria "Programação para Dispositivos Móveis"
* do curso superior de tecnologia em Análise e Desenvolvimento de Sistemas
* Projeto: Snake Game
* */
// CLASSE DE DADOS PARA DEFINIR O TAMANHO DA SNAKE
data class State(val food: Pair<Int,Int>, val snake: List<Pair<Int,Int>>)

// CLASSE QUE DEFINE A LÓGICA DE JOGO
class Game(val scope: CoroutineScope){
    var score = 0
    companion object {
        const val BOARD_SIZE = 16
    } val mutex = Mutex()
    val mutableState =
        MutableStateFlow(State(food= Pair(5,5), snake = listOf(Pair(7,7))))
    val state: Flow<State> = mutableState


    var move = Pair(1,0)
        set(value){
            scope.launch {
                mutex.withLock {
                    field = value
                }
            }
        }

    init {
        scope.launch {
            var snakeLenght = 4
            while (true){
                delay(150)
                mutableState.update {
                    val newPosition = it.snake.first().let { pos ->
                        mutex.withLock {
                            Pair(
                                (pos.first + move.first + BOARD_SIZE) % BOARD_SIZE,
                                (pos.second + move.second + BOARD_SIZE) % BOARD_SIZE
                            )

                        }
                    }
                    if (newPosition == it.food){
                        snakeLenght++
                        score++
                    }
                    if(it.snake.contains(newPosition)){
                        snakeLenght = 4
                        score = 0
                    }
                    it.copy(
                        food = if(newPosition == it.food) Pair(
                            Random.nextInt(BOARD_SIZE),
                            Random.nextInt(BOARD_SIZE)
                        )else it.food,
                        snake = listOf(newPosition) + it.snake.take(snakeLenght - 1)
                    )
                }
            }
        }
    }
}

// MONTAGEM DA INTERFACE E CONEXÃO COM A LÓGICA DO JOGO
@Composable
fun Snake(game: Game) {
    val state = game.state.collectAsState(initial = null)
    var score = game.score
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        state.value?.let {
            Board(it)
        }

        Buttons {
            game.move = it
        }
        Text(text = "Score: $score", fontWeight = FontWeight.Bold)
    }
}

// CLASSE DE DEFINIÇÃO DOS CONTROLES DO JOGO
@Composable
fun Buttons(onDirectionChange: (Pair<Int,Int>) -> Unit) {
    val buttonSize = Modifier.size(64.dp)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(24.dp)) {
        Button(onClick = {onDirectionChange(Pair(0,-1))}, modifier = buttonSize) {
            androidx.compose.material3.Icon(Icons.Default.KeyboardArrowUp,null)
        }
        Row{
            Button(onClick = {onDirectionChange(Pair(-1,0))}, modifier = buttonSize) {
                androidx.compose.material3.Icon(Icons.Default.KeyboardArrowLeft,null)
            }
            Box(modifier = Modifier.size(40.dp))
            Button(onClick = {onDirectionChange(Pair(1,0))}, modifier = buttonSize) {
                androidx.compose.material3.Icon(Icons.Default.KeyboardArrowRight,null)
            }
        }
        Button(onClick = {onDirectionChange(Pair(0,1))}, modifier = buttonSize) {
            androidx.compose.material3.Icon(Icons.Default.KeyboardArrowDown,null)
        }
    }
}

// CLASSE DE DEFINIÇÃO DO CAMPO
@Composable
fun Board(state: State) {
    BoxWithConstraints(Modifier.padding(16.dp)) {
        val tileSize = maxWidth / Game.BOARD_SIZE

        Box(
            Modifier
                .size(maxWidth)
                .border(2.dp, Color.DarkGray)
        )
        Box(
            Modifier
                .offset(x = tileSize * state.food.first, y = tileSize * state.food.second)
                .size(tileSize)
                .background(
                    Color.Red, CircleShape
                )
        )
        state.snake.forEach {
            Box(modifier = Modifier
                .offset(x = tileSize * it.first, y = tileSize * it.second)
                .size(tileSize)
                .background(
                    Color.DarkGray, CircleShape
                ))
        }
    }
}
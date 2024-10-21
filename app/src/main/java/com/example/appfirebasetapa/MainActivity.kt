package com.example.appfirebasetapa

import android.os.Bundle
import android.util.Log
import android.util.Log.d
import android.util.Log.w
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.appfirebasetapa.ui.theme.AppFirebaseTAPATheme
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class MainActivity : ComponentActivity() {
    // Instancia o Firestore
    private val db: FirebaseFirestore by lazy {
        Firebase.firestore
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inicializa o Firebase
        FirebaseApp.initializeApp(this)

        enableEdgeToEdge()
        setContent {
            AppFirebaseTAPATheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFFEDE7F6) // Fundo lilás claro
                ) {
                    App(db)
                }
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun App(db: FirebaseFirestore) {
        var nome by remember { mutableStateOf("") }
        var telefone by remember { mutableStateOf("") }
        var clientes by remember { mutableStateOf<List<Cliente>>(emptyList()) }
        var editandoClienteId by remember { mutableStateOf<String?>(null) }
        var telefoneErro by remember { mutableStateOf(false) }

        // Função para ler clientes do Firestore
        fun lerClientes() {
            db.collection("Clientes")
                .get()
                .addOnSuccessListener { documents ->
                    clientes = documents.map { document ->
                        Cliente(
                            id = document.id,
                            nome = document.getString("nome") ?: "",
                            telefone = document.getString("telefone") ?: ""
                        )
                    }
                }
                .addOnFailureListener { e ->
                    Log.w("Firestore", "Error getting documents.", e)
                }
        }

        // Função para excluir um cliente
        fun excluirCliente(clienteId: String) {
            db.collection("Clientes").document(clienteId).delete()
                .addOnSuccessListener {
                    Log.d("Firestore", "Cliente excluído com sucesso!")
                    lerClientes()
                }
                .addOnFailureListener { e ->
                    Log.w("Firestore", "Erro ao excluir documento", e)
                }
        }

        // Função para editar um cliente
        fun editarCliente(clienteId: String, novoNome: String, novoTelefone: String) {
            val clienteAtualizado = hashMapOf(
                "nome" to novoNome,
                "telefone" to novoTelefone
            )
            db.collection("Clientes").document(clienteId).set(clienteAtualizado)
                .addOnSuccessListener {
                    Log.d("Firestore", "Cliente atualizado com sucesso!")
                    lerClientes()
                    nome = ""
                    telefone = ""
                    editandoClienteId = null
                }
                .addOnFailureListener { e ->
                    Log.w("Firestore", "Erro ao atualizar documento", e)
                }
        }

        // Função para validar o telefone
        fun validarTelefone(telefone: String): Boolean {
            return telefone.all { it.isDigit() } && telefone.length >= 8
        }

        LaunchedEffect(Unit) {
            lerClientes()
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(text = "Gestão de Clientes", color = Color.White) },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = Color(0xFF6A1B9A) // Roxo
                    )
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                OutlinedTextField(
                    value = nome,
                    onValueChange = { nome = it },
                    label = { Text("Nome") },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Person, contentDescription = "Nome")
                    },
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = Color(0xFF6A1B9A), // Roxo
                        cursorColor = Color(0xFF6A1B9A) // Roxo
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = telefone,
                    onValueChange = {
                        if (it.all { char -> char.isDigit() }) {
                            telefone = it
                            telefoneErro = !validarTelefone(it)
                        }
                    },
                    isError = telefoneErro,
                    label = { Text("Telefone") },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Phone, contentDescription = "Telefone")
                    },
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = Color(0xFF6A1B9A), // Roxo
                        cursorColor = Color(0xFF6A1B9A), // Roxo
                        errorBorderColor = MaterialTheme.colorScheme.error
                    ),
                    keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Phone)
                )

                if (telefoneErro) {
                    Text(
                        text = "Número de telefone inválido",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                ElevatedButton(
                    onClick = {
                        if (telefoneErro) return@ElevatedButton

                        if (editandoClienteId != null) {
                            editarCliente(editandoClienteId!!, nome, telefone)
                        } else {
                            val pessoas = hashMapOf(
                                "nome" to nome,
                                "telefone" to telefone
                            )
                            db.collection("Clientes").add(pessoas)
                                .addOnSuccessListener { documentReference ->
                                    Log.d(
                                        "Firestore",
                                        "Documento adicionado com ID: ${documentReference.id}"
                                    )
                                    lerClientes()
                                    nome = ""
                                    telefone = ""
                                }
                                .addOnFailureListener { e ->
                                    Log.w("Firestore", "Erro ao adicionar documento", e)
                                }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.elevatedButtonColors(
                        containerColor = Color(0xFFBA68C8) // Roxo claro
                    )
                ) {
                    Text(
                        text = if (editandoClienteId != null) "Atualizar Cliente" else "Cadastrar Cliente",
                        color = Color.White // Texto branco
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                LazyColumn {
                    items(clientes) { cliente ->
                        ElevatedCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .border(
                                    width = 2.dp,
                                    color = Color(0xFF6A1B9A), // Cor da borda rosa
                                    shape = RoundedCornerShape(12.dp) // Borda arredondada
                                ),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFFEDE7F6)
                            )
                        ) {
                            // Adicione padding aqui para os textos não ficarem colados na borda
                            Column(
                                modifier = Modifier.padding(16.dp) // Padding ao redor do conteúdo
                            ) {
                                Text(
                                    text = "Nome: ${cliente.nome}",
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        color = Color(0xFF262525)
                                    )
                                )
                                Spacer(modifier = Modifier.height(4.dp)) // Espaço entre os textos
                                Text(
                                    text = "Telefone: ${cliente.telefone}",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = Color(0xFF262525)
                                    )
                                )
                                Spacer(modifier = Modifier.height(8.dp)) // Espaço antes da linha de botões

                                Row(
                                    modifier = Modifier.fillMaxWidth(), // Ocupa toda a largura
                                    horizontalArrangement = Arrangement.End // Alinha os botões à direita
                                ) {
                                    TextButton(onClick = {
                                        nome = cliente.nome
                                        telefone = cliente.telefone
                                        editandoClienteId = cliente.id
                                    }) {
                                        Icon(
                                            imageVector = Icons.Default.Edit,
                                            contentDescription = "Editar",
                                            tint = Color(0xFF6A1B9A)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            "Editar",
                                            color = Color(0xFF6A1B9A)
                                        ) // Cor do texto do botão
                                    }

                                    Spacer(modifier = Modifier.width(8.dp)) // Espaço entre os botões

                                    TextButton(onClick = { excluirCliente(cliente.id) }) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Excluir",
                                            tint = Color(0xFF6A1B9A)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            "Excluir",
                                            color = Color(0xFF6A1B9A)
                                        ) // Cor do texto do botão
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

                // Classe de dados para Cliente
data class Cliente(
    val id: String,
    val nome: String,
    val telefone: String
)

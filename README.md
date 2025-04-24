
# Tris Game (Tic Tac Toe)

Un'applicazione Java completa del classico gioco Tris (Tic Tac Toe), con interfaccia grafica sviluppata in Swing. Supporta modalità  single player con due livelli di difficoltà  (facile/difficile), modalità  multiplayer con connessione sicura via SSL e funzionalità  di login/registrazione con salvataggio delle statistiche su database MySQL, usando XAMPP.

## Funzionalità  principali

- **Interfaccia grafica** intuitiva (Swing)
- **Login e registrazione** utenti
- Modalità **Single Player**:
  - Difficoltà **Facile** (mosse casuali)
  - Difficoltà **Difficile** (algoritmo Minimax)
- Modalità **Multiplayer** via rete (SSL)
- Salvataggio delle **statistiche** (vittorie, sconfitte, pareggi)
- Logging tramite **Log4j 2**

## Struttura database

Il gioco si connette a un database MySQL per gestire gli utenti. È necessaria la seguente tabella:

```sql
CREATE TABLE users (
    username VARCHAR(50) PRIMARY KEY,
    password VARCHAR(100) NOT NULL,
    vittorie INT DEFAULT 0,
    sconfitte INT DEFAULT 0,
    pareggi INT DEFAULT 0
);
```

- `username`: identificativo univoco dell'utente
- `password`: password criptata con SHA-256
- `vittorie`, `sconfitte`, `pareggi`: statistiche di gioco

## Funzionalità  Principali

### Login e Registrazione
- Gli utenti possono creare un account con username e password
- Dopo l'accesso, vengono caricate le statistiche personali

### Modalità  Single Player
- **Facile**: la CPU effettua mosse casuali
- **Difficile**: la CPU usa l'algoritmo **Minimax** per giocare ottimamente

### Modalità  Multiplayer
- Connessione sicura tramite **SSL/TLS**
- Uno dei due utenti avvia un server, l'altro si connette

### Statistiche
- Ogni partita aggiorna automaticamente vittorie/sconfitte/pareggi
- Le statistiche sono mostrate all'utente dopo il login

### Logging
- File di configurazione `log4j2.xml`
- Logging su console
- Livelli configurabili: info, debug, error, ecc.

## Struttura del Progetto

```
tris/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── 
│   │   │       ├── database/
│   │   │       ├── gui/
│   │   │       ├── model/
│   │   │       ├── network/
│   │           └── resources/
│   │       		└── log4j2.xml
├── pom.xml
├── client-truststore.jks
├── server-cert.cer
├── server-keystore.jks
```

## Requisiti

- **Java 8**
- **Swing** per GUI
- **MySQL/XAMMP** per gestione utenti/statistiche
- **JDBC** per connettività  DB
- **SSL/TLS** per comunicazioni sicure in multiplayer
- **Log4j 2** per logging
- **Maven** per la gestione delle dipendenze

## Build & Esecuzione

1. **Clona il repository**
   ```bash
   git clone https://github.com/ale-cappe06/tris.git
   cd tris
   ```

2. **Configura il Database MySQL**
   ```sql
   CREATE DATABASE tris_game;
   USE tris_game;

   CREATE TABLE users (
       username VARCHAR(50) PRIMARY KEY,
       password VARCHAR(255),
       vittorie INT DEFAULT 0,
       sconfitte INT DEFAULT 0,
       pareggi INT DEFAULT 0
   );
   ```

3. **Configura `DBConnection.java`**
   Imposta correttamente le tue credenziali e URL del database nel file di connessione:
   ```java
   String url = "jdbc:mysql://localhost:3306/tris_game";
   String user = "root";
   String password = "";
   ```

4. **Costruisci il progetto con Maven**
   ```bash
   mvn clean install
   ```

5. **Esegui il gioco**
   ```bash
   java -jar target/tris-1.0-SNAPSHOT.jar
   ```

## Sicurezza

La modalità  multiplayer utilizza **SSL/TLS** per cifrare la comunicazione tra i client.
Le credenziali degli utenti sono validate tramite accesso sicuro al database.

### Certificati SSL/TLS

Per la modalità  multiplayer:
- Usa _keytool_ per generare i certificati se non forniti
- Entrambi i giocatori devono disporre di certificati validi

```bash
keytool -genkeypair -alias server -keyalg RSA -keystore server.keystore -storepass password
```

## Contribuisci

Hai idee per migliorare il gioco? Sentiti libero di aprire una pull request o segnalare problemi via issue.

## Licenza

Questo progetto è distribuito sotto **licenza MIT**.

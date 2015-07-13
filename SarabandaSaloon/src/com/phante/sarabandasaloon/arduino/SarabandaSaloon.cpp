/*
 * Copyright 2015 Elvis Del Tedesco
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*
 Sarabanda Saloon Library
 autore: Elvis Del Tedesco
 data: 8/03/2014

 Libreria comune per la gestione del Sarabanda Saloon
 */

#include "SarabandaSaloon.h"
//#include <Arduino.h>
//#include <Wire.h>                 // Comunicazione SPI
//#include <LiquidCrystal_I2C.h>    // Libreria per cristalli liquidi
//#include <UIPEthernet.h>          // Libreria per ENC28J60

// Funzione per il reset remoto dell'arduino
void(* RESET)(void) = 0;

/**
 * Inizializza il controller
 */
void SarabandaSaloon::begin(LiquidCrystal_I2C *lcd, bool isMaster) {
	_lcd = lcd;

	// Inizializza lo stato del gioco
	for (byte i = 0; i < 4; i++)
		_gameStatus[i] = '-';

	_gamePinEnabled = 1;
	_demoIsActive = 0;

	_isMaster = isMaster;
}

/**
 * Imposta le variabili interne alla classe per i pulsanti di controllo
 * per la gestione del debouce e inizializza lo stato fisico dei pin
 */
void SarabandaSaloon::setControllerPin(byte pinNumber, byte* pin) {
	_controllerPinNumber = pinNumber;
	_controllerPin = pin;
	_controllerPinState = new byte(_controllerPinNumber);
	_controllerPinLastState = new byte(_controllerPinNumber);
	_controllerPinLastDebounceTime = new long(_controllerPinNumber);

	for (byte i = 0; i < _controllerPinNumber; i++) {
		_controllerPinState[i] = HIGH;
		_controllerPinLastDebounceTime[i] = 0;
		// Inposta il pin in ingresso
		pinMode(_controllerPin[i], INPUT);
		// Accende il resistore di pullup
		digitalWrite(_controllerPin[i], HIGH);
	}
}

/**
 * Imposta le variabili interne alla classe per i pulsanti di gioco
 * per la gestione del debouce e inizializza lo stato fisico dei pin
 */
void SarabandaSaloon::setGamePin(byte* pin) {
	_gamePin = pin;

	for (byte i = 0; i < 4; i++) {
		_gamePinState[i] = HIGH;
		_gamePinLastDebounceTime[i] = 0;
		// Inposta il pin in ingresso
		pinMode(_gamePin[i], INPUT);
		// Accende il resistore di pullup
		digitalWrite(_gamePin[i], HIGH);
	}
}

/**
 * Imposta lo stato dei rele
 */
void SarabandaSaloon::setRelePin(byte* selected, byte* error, byte* optionOne, byte* optionTwo) {
	_selectedRele = selected;
	_errorRele = error;
	_optionOneRele = optionOne;
	_optionTwoRele = optionTwo;

	// Inizializza le uscite per la gestione dei rele
	for (byte i = 0; i < 4; i++) {
		// Inizializza i rele di selezione
		pinMode(_selectedRele[i], OUTPUT);
		digitalWrite(_selectedRele[i], HIGH);

		// Inizializza i rele di errore
		pinMode(_errorRele[i], OUTPUT);
		digitalWrite(_errorRele[i], HIGH);

		// Inizializza i rele opzionali 1
		pinMode(_optionOneRele[i], OUTPUT);
		digitalWrite(_optionOneRele[i], HIGH);

		// Inizializza i rele opzionali 2
		pinMode(_optionTwoRele[i], OUTPUT);
		digitalWrite(_optionTwoRele[i], HIGH);
	}
}

/**
 * Gestisce la logica di pressione dei pulsanti di controllo
 */
void SarabandaSaloon::runControllerPin() {
	// Verifica quale è il pulsante premuto
	if (controllerPinDebouce(0))
		onDemoButton();

	if (controllerPinDebouce(1))
		onErrorButton();

	if (controllerPinDebouce(2))
		onResetButton();

	if (controllerPinDebouce(3))
		onFullResetButton();
}

/**
 * Gestisce la pressione dei pulsanti di gioco il cui stato può essere
 *
 * - Non premuto (Abilitato)
 * O Premuto (Disabilitato)
 * X Errore (Disabilitato)
 * D Disabilitato
 *
 * TODO: idea per il futuro, per rendere meno predicibile la priorità dei pulsanti nella pressione in fase di reset totale
 *	si potrebbe andare a popolare in modo casuale un vettore di 4 elementi che indica la sequenza di valutazione dei pulsanti
 *
 */
void SarabandaSaloon::runGamePin() {
	if (_gamePinEnabled == 1) {
		for (byte i = 0; i < 4; i ++) {
			if ((getButtonStatus(i) == '-') && (gamePinDebouce(i) == 1)) {
				// Imposto lo stato del pulsante
				setButtonStatus(i, 'O');

				loadButtonStatus();

				// Disabilita i pulsanti
				_gamePinEnabled = 0;
				break;
			}
		}
	}
}

/*
 * Gestisce la pressione del pulsante ERRORE
 */
void SarabandaSaloon::onErrorButton() {
	sendError();
	onError();
}

/*
 * Gestisce la ricezione del comando ERRORE
 */
void SarabandaSaloon::onErrorMessage() {
	onError();
}

/**
 * Gestisce il comando ERRORE
 */
void SarabandaSaloon::onError() {
	if (_isMaster) {
		// Imposta l'ultimo pulsante premuto in stato ERRORE
		for (byte i = 0; i < 4; i++) {
			// Imposta lo stato del pulsante
			if (getButtonStatus(i) == 'O') setButtonStatus(i, 'X');
		}

		loadButtonStatus();

		//Riabilita la pressione dei pulsanti
		_gamePinEnabled = 1;
	}
}

/**
 * Gestisce la pressione del pulsante RESET TOTALE
 */
void SarabandaSaloon::onFullResetButton() {
	sendFullReset();
	onFullReset();
}

/**
 * Gestisce la ricezione del comando RESET TOTALE
 */
void SarabandaSaloon::onFullResetMessage() {
	onFullReset();
}

/**
 * Gestisce il comando RESET TOTALE
 */
void SarabandaSaloon::onFullReset() {
	if (_isMaster) {
		// Azzero lo stato dei pulsanti
		for (byte i = 0; i < 4; i++) {
			setButtonStatus(i, '-');
		}

		loadButtonStatus();

		//Riabilita la pressione dei pulsanti
		_gamePinEnabled = 1;
	}
}

/**
 * Gestisce la pressione del pulsante RESET
 */
void SarabandaSaloon::onResetButton() {
	sendReset();
	onReset();
}

/**
 * Gestisce la ricezione del comando RESET
 */
void SarabandaSaloon::onResetMessage() {
	onReset();
}

/**
 * Gestisce il comando RESET
 */
void SarabandaSaloon::onReset() {
	if (_isMaster) {
		// Master
		// Azzero lo stato dei pulsanti premuti
		for (byte i = 0; i < 4; i++) {
			if (getButtonStatus(i) == 'O') setButtonStatus(i, '-');
		}

		loadButtonStatus();

		//Riabilita la pressione dei pulsanti
		_gamePinEnabled = 1;
	}
}

/**
 * Gestisce la pressione del pulsante DEMO
 */
void SarabandaSaloon::onDemoButton() {
	sendDemo();
	onDemo();
}

/**
 * Gestisce la ricezione del comando DEMO
 */
void SarabandaSaloon::onDemoMessage() {
	onDemo();
}

/**
 * Gestisce il comando DEMO
 */
void SarabandaSaloon::onDemo() {
	if (_isMaster) {
		setButtonStatus(0,'O');
		setButtonStatus(1, 'O');
		setButtonStatus(2, 'O');
		setButtonStatus(3, 'O');
		loadButtonStatus();

		// Attiva la modalità demo
		_demoIsActive = 1;

		//Disabilita la pressione dei pulsanti
		_gamePinEnabled = 0;
	}
}

/**
 * Avvia la scheda ethernet (UIPEthernet.h)
 */
/* void SarabandaSaloon::startEthernet(byte macAddress[], byte ipAddress[]) {
	// Inizializza la rete
	print("Ethernet Init ...", 2);

	// Prova ad inizializzare con il DHCP
	 if (Ethernet.begin(macAddress) == 0) {
		print("No DHCP ...", 2);
		Ethernet.begin(macAddress, ipAddress);
		print("IP(S):", 2);
	} else {
		print("IP(D):", 2);
	}

	//Mostra l'ip
	_lcd->setCursor(6, 2);
	_lcd->print(Ethernet.localIP());
}*/

/**
 * Avvia il server UDP (UIPEthernet.h)
 */
/*int SarabandaSaloon::startUDPServer(EthernetUDP *UDPChannel) {

	// Inizializza il server UDP
	//print("Listener is starting", 3);
	byte i = 0;
	byte serverUDPinit = 0;
	do {
		serverUDPinit = UDPChannel->begin(UDP_PORT);
	} while ((serverUDPinit == 0) || (i++ < 100));

	return serverUDPinit;
}*/

/**
 * Scrive sul display la riga completa
 */
void SarabandaSaloon::print(const char message[], byte row, byte column) {
	_lcd->setCursor(column, row);
	_lcd->print("                    ");
	_lcd->setCursor(column, row);
	_lcd->print(message);
}

/**
 * Pulisce il display
 */
void SarabandaSaloon::clear() {
	_lcd->clear();
}

/**
 * Debounce dei pulsanti di controllo
 */
int SarabandaSaloon::controllerPinDebouce(int button) {
	return debounceDigital(button, _controllerPin, _controllerPinState,
			_controllerPinLastState, _controllerPinLastDebounceTime);
}

/**
 * Debounce dei pulsanti di gioco
 */
int SarabandaSaloon::gamePinDebouce(int button) {
	return debounceDigital(button, _gamePin, _gamePinState, _gamePinLastState,
			_gamePinLastDebounceTime);
}

/**
 * Gestisce il debouce del pin digitale generico, richiede per contro il passaggio di tutti i parametri necessari
 */
int SarabandaSaloon::debounceDigital(int button, byte* pin, byte *pinState,
		byte *pinLastState, long *pinLastDebounceTime) {
	// Legge lo stato del pulsante
	byte reading = digitalRead(pin[button]);

	// Verifica se il cambio stato e' una vera pressione oppure rumore di fondo
	if (reading != pinLastState[button]) {
		// Resetta il timer del debounce
		pinLastDebounceTime[button] = millis();
	}

	if ((millis() - pinLastDebounceTime[button]) > DEBOUNCE_DELAY) {
		// verifica se lo stato del pulsante e' cambiato
		if (reading != pinState[button]) {
			pinState[button] = reading;

			// Esci dalla funzione e torna true se il pulsante e' premuto
			if (pinState[button] == LOW) {
				return 1; //Pulsante premuto esco dalla funzione 
			}
		}
	}

	pinLastState[button] = reading;
	return 0;
}

/**
 * Ottiene lo stato dei pulsanti
 */
char SarabandaSaloon::getButtonStatus(byte i) {
	return _gameStatus[i];
}

/**
 * Imposta lo stato dei pulsanti
 */
void SarabandaSaloon::setButtonStatus(byte i, char newStatus) {
	// Stati pulsante
	// - Non premuto
	// O premuto
	// X errore
	// D Disabiliato
	_gamePreviousStatus[i] = _gameStatus[i];
	_gameStatus[i] = newStatus;
}

void SarabandaSaloon::loadButtonStatus() {
	showButtonStatus();
	showReleStatus();
	if (_isMaster) sendButtonStatus();
}

/**
 * Mostra lo status dei pulsanti di gioco sul lcd e attiva gli eventuali rele
 */
void SarabandaSaloon::showButtonStatus() {
	// Stati pulsante
	// - Non premuto
	// O premuto
	// X errore
	// D Disabiliato

	print("", 2);
	print("", 3);
	for (int i = 0; i < 4; i++) {
		int column = 3+(i*(2+2));
		switch (_gameStatus[i]) {
		case '-':
			_lcd->setCursor(column, 2);
			_lcd->print("__");
			break;
		case 'X':
			_lcd->setCursor(column, 2);
			_lcd->print("xx");
			_lcd->setCursor(column, 3);
			_lcd->print("xx");
			break;
		case 'O':
			_lcd->setCursor(column, 2);
			_lcd->print("OO");
			_lcd->setCursor(column, 3);
			_lcd->print("OO");
			break;
		case '#':
			_lcd->setCursor(column, 2);
			_lcd->print("##");
			_lcd->setCursor(column, 3);
			_lcd->print("##");
			break;
		default:
			_lcd->setCursor(column, 2);
			_lcd->print("??");
			_lcd->setCursor(column, 3);
			_lcd->print("??");
			break;
		}
	}
}

/**
 * Modifica lo stato dei rele
 */
void SarabandaSaloon::showReleStatus() {
	for (int i = 0; i < 4; i++) {
		if ( _gameStatus[i] != _gamePreviousStatus[i]) {
			switch (_gameStatus[i]) {
				case '-':
					digitalWrite(_selectedRele[i], HIGH);
					digitalWrite(_errorRele[i], HIGH);
					digitalWrite(_optionOneRele[i], HIGH);
					digitalWrite(_optionTwoRele[i], HIGH);
					break;
				case 'X':
					digitalWrite(_selectedRele[i], HIGH);
					digitalWrite(_errorRele[i], LOW);
					digitalWrite(_optionOneRele[i], HIGH);
					digitalWrite(_optionTwoRele[i], HIGH);
					break;
				case 'O':
					digitalWrite(_selectedRele[i], LOW);
					digitalWrite(_errorRele[i], HIGH);
					digitalWrite(_optionOneRele[i], HIGH);
					digitalWrite(_optionTwoRele[i], LOW);
					break;
				case '#':
					digitalWrite(_selectedRele[i], HIGH);
					digitalWrite(_errorRele[i], HIGH);
					digitalWrite(_optionOneRele[i], LOW);
					digitalWrite(_optionTwoRele[i], HIGH);
					break;
				default: break;
			}
		}
	}
}

/**
 * Riceve un comando MASTER, quindi MR o MO
 */
void SarabandaSaloon::receiveMaster(char message[]) {
	if (!_isMaster) {
		print(message, 0);

		char firstCommandCharacter = message[1];

		switch (firstCommandCharacter) {
			case 'R': // Messaggio MASTERSTART
				sendSlaveRegistration();
				break;
			case 'O': // Messaggio MASTERSLAVEOK
				break;
			default: // Nessun messaggio riconosciuto
				break;
		}
	}
}

/**
 *	MR - Master start - MASTER to SLAVE in fase di avvio, richiede la registrazione degli SLAVE
 */
void SarabandaSaloon::sendMasterStart() {
	char message[] = "MR-MASTERSTART";
	sendUDPMessage(message);
}

/**
 * MO - slave registration Ok - MASTER to SLAVE per confermare la registrazione
 *
 * TODO Inviare il messaggio solo allo slave specifico (con un id nel messaggio) oppure direttamente all'ip dello stesso
 */
void SarabandaSaloon::sendSlaveRegistrationOK() {
	char message[] = "MO-SLAVEOK";
	sendUDPMessage(message);
}

/**
 * Riceve un pacchetto SLAVE quindi SL o SD
 */
void SarabandaSaloon::receiveSlave(char message[]) {
	if (_isMaster) {
			print(message, 0);

			char firstCommandCharacter = message[1];

			switch (firstCommandCharacter) {
				case 'L': // Messaggio SLAVEREGISTRATION
					sendSlaveRegistrationOK();
					sendButtonStatus();
					break;
				case 'D': // Messaggio SLAVEDEREGISTRATION
					break;
				default: // Nessun messaggio riconosciuto
					break;
			}
		}
}

/**
 * SL - slave Registration - SLAVE to MASTER per avvisare della loro connessione
 */
void SarabandaSaloon::sendSlaveRegistration() {
	char message[] = "SL-SLAVEREG";
	sendUDPMessage(message);
}

/**
 * SD - slave De-registratiomn - SLAVE to MASTER per indicare al master la chiusura dello slave
 */
void SarabandaSaloon::sendSlaveDeRegistration() {
	char message[] = "SD-SLAVEDEREG";
	sendUDPMessage(message);
}

/**
 * B - Button - MASTER to SLAVE per indicare lo stato dei pulsanti di gioco
 * 				SLAVE to MASTER per forzare uno specifico stato sul master
 */
void SarabandaSaloon::receiveButtom(char message[]) {
	print(message, 0);

	for (byte j = 0; j < 4; j++)
		setButtonStatus(j, message[j+1]);

	loadButtonStatus();
}

void SarabandaSaloon::sendButtonStatus() {
	char message[] = "B----";
	for (byte j = 0; j < 4; j++) message[j+1] = getButtonStatus(j);

	sendUDPMessage(message);
}

/**
 * FULLRESET - Full Reset - SLAVE to MASTER per comandere il reset totale dello stato dei pulsanti
 */
void SarabandaSaloon::sendFullReset() {
	char message[] = "FULLRESET";
	sendUDPMessage(message);
}

/**
 * RESET - Reset - SLAVE to MASTER per comandare lo sblocco dell'ultimo pulsante premuto ma non il reset degli stati
 */
void SarabandaSaloon::sendReset() {
	char message[] = "RESET";
	sendUDPMessage(message);
}

/**
 * ERROR - Error - SLAVE to MASTER per comandere di impostare in stato IN ERRORE all'ultimo pulsante premuto
 */
void SarabandaSaloon::sendError() {
	char message[] = "ERROR";
	sendUDPMessage(message);
}

/**
 * DEMO - Demo - SLAVE to MASTER per comandare l'avvio della mosalità demo
 */
void SarabandaSaloon::sendDemo() {
	char message[] = "DEMO";
	sendUDPMessage(message);
}

/**
 * X - Reset della macchina - SLAVE to MASTER per comandare il reset del master
 */
void SarabandaSaloon::sendRESET() {
	char message[] = "X";
	sendUDPMessage(message);
}

/**
 * Invia sulla rete il messaggio di output
 */
void SarabandaSaloon::sendUDPMessage(const char message[]) {
	char outMessage[UDP_PACKET_MAX_SIZE];
	memset(outMessage, 0, UDP_PACKET_MAX_SIZE);

	// Concatena l'header per identificare i messaggi9
	strcat(outMessage, messageHeader);
	strcat(outMessage, message);
	print(message, 1);

	// UIPEthernet.h
	/*_UDPChannel.beginPacket(BROADCAST_IP, UDP_PORT);
	Serial.print("> ");
	Serial.println(outMessage);\
	_UDPChannel.write(outMessage);
	_UDPChannel.endPacket();
	*/

#ifdef DEBUG
  //IPAddress src(BROADCAST_IP[0], BROADCAST_IP[1], BROADCAST_IP[2], BROADCAST_IP[3]);
  //Serial.print(src);
  Serial.print(":");
  Serial.print(UDP_PORT);
  Serial.print(" <- (");
  Serial.print(sizeof(outMessage));
  Serial.print(") ");
  Serial.println(outMessage);
#endif

	// EtherCard.h
	ether.sendUdp(outMessage, sizeof(outMessage), UDP_PORT, BROADCAST_IP, UDP_PORT );
}

/**
 * Verifica che l'intestazione del messaggio sia quella attesa del sarabanda
 */
int SarabandaSaloon::isASarabandaSaloonMessage(const char *message) {
	char *inMessageHeader = new char(strlen(messageHeader));
	strncpy(inMessageHeader, message, strlen(messageHeader));
	if (strcmp (messageHeader,inMessageHeader) != 0) return 1;
	return 0;
}

/**
 * Elimina l'header del messaggio dal messaggio
 */
void SarabandaSaloon::trimHeaderFromMessage(char destination[], const char *message, unsigned int len) {
	for (unsigned int i = 0; i < len-strlen(messageHeader); i++) {
		destination[i] = message[i+strlen(messageHeader)];
	}
}

/**
 * Effettua il parsing dei messaggi in ingresso
 */
void SarabandaSaloon::parseMessage(const char *message, unsigned int messageSize) {
		if (isASarabandaSaloonMessage(message)) {
			char command[messageSize];
			memset(command, 0, messageSize);
			trimHeaderFromMessage(command, message, messageSize);

			char firstCommandCharacter = command[0];

			print(command, 0);

			switch (firstCommandCharacter) {
				case 'M': // Messaggio MASTER
					receiveMaster(command);
					break;
				case 'S': // Messaggio SLAVE
					receiveSlave(command);
					break;
				case 'B': // Messaggio BUTTON
					receiveButtom(command);
					break;
				case 'F': // Messaggio FULL RESET
					onFullResetMessage();
					break;
				case 'R': // Messaggio RESET
					onResetMessage();
					break;
				case 'E': // Messaggio ERROR
					onErrorMessage();
					break;
				case 'D': // Messaggio DEMO
					onDemoMessage();
					break;
				case 'X':
					if (_isMaster) RESET();
					break;
				default: // Nessun messaggio riconosciuto
					break;
			}

		/**
		 * Messaggi di rete
		 *
		 * MR - Master start - MASTER to SLAVE in fase di avvio, richiede la registrazione degli SLAVE,
		 * 		lo slave deve rispondere con uno SL (SLAVE REGISTRATION)
		 * SL - slave Registration - SLAVE to MASTER per avvisare della loro connessione,
		 * 		il master risponde con un MO (SLAVE REGISTRATION OK)
		 * MO - slave registration OK - MASTER to SLAVE per confermare la registrazione
		 * SD - slave De-registratiomn - SLAVE to MASTER per indicare al master la chiusura dello slave
		 * 		il master non risponde a questo messaggio, si ipotizza che lo slave sia diventato irreperibile immediatamente
		 * B - Button - MASTER to SLAVE per indicare lo stato dei pulsanti di gioco, lo SLAVE prende in carico il messaggio e non risponde
		 * 				SLAVE to MASTER per forzare uno specifico stato sul master,
		 * 				il master risponde con un B (BUTTON) indicando lo stato attuale dei pulsanti
		 * F - Full Reset - SLAVE to MASTER per comandere il reset totale dello stato dei pulsanti
		 * 		il master risponde con un B (BUTTON) indicando lo stato attuale dei pulsanti
		 * R - Reset - SLAVE to MASTER per comandare lo sblocco dell'ultimo pulsante premuto ma non il reset degli stati
		 * 		il master risponde con un B (BUTTON) indicando lo stato attuale dei pulsanti
		 * E - Error - SLAVE to MASTER per comandere di impostare in stato IN ERRORE all'ultimo pulsante premuto
		 * 		il master risponde con un B (BUTTON) indicando lo stato attuale dei pulsanti
		 * D - Demo - SLAVE to MASTER per comandare l'avvio della modalità demo,
		 * 		nessuna risposta da parte del master (opzionalmente bombarda la rete con messaggio B (BUTTON)
		 */



	}

	// Verifica che non si tratti di un pacchetto "vagante", controllo forse non necessario ma meglio abbondare :-)
	/*		if (UDPPacketBuffer[0] == 'S') {

				char command = UDPPacketBuffer[1];

				switch (command) {
				// M - MASTER START - inviato dal master in fase di avvio, richiede la registrazione degli SLAVE
				case 'M':
					// Memorizzo l'ip del master
					//masterIP = senderIP;

					// Segno come non registrato
					//isRegistered = false;

					// Invio in rete il messaggio di registrazione dello slave
					controller.sendSlaveRegistration();

					controller.print("WOW! A New Master!!");
					controller.print("Wait master response", 1);
					break;
					// R - SLAVE REGISTRATION - inviato dagli slave per avvisare della loro connessione
				case 'R':
					// TODO Aggiungo lo slave alla lista degli slave
					//slaveIP[0] = senderIP;
					controller.print("A New Slave!!");
					break;
					// O - SLAVE REGISTRATION OK
				case 'O':
					//if (!isRegistered) {
						//masterIP = senderIP;
						//isRegistered = true;

						// Con la registrazione il master invia anche lo stato inziale
						for (byte n = 2; n < 6; n++)
							//SarandaButtonStatus[n - 2] = UDPPacketBuffer[n];

						controller.clear();
						controller.print("Registration is OK!");
						//showSarabandaStatus();
					//} else {
						controller.print("Registration? Why?");
					// }
					break;
					// D - SLAVE DEREGISTRATION
				case 'D':
					// TODO Uno slave si √® deregistrato, lo elimino dalla lista
					//slaveIP[0] = senderIP;
					controller.print("Slave DERegistration");
					break;
					// B - BUTTON
				case 'B':
					// Messaggio dal master sullo stato dei pulsanti
					//if (senderIP == masterIP) {
						controller.print("BTN From Master");
						// Il messaggio arriva dal master quindi detta legge
						for (byte n = 2; n < 6; n++)
							//SarandaButtonStatus[n - 2] = UDPPacketBuffer[n];
					//} else {
						controller.print("BTN From Slave");
						// Il messaggio arriva da uno slave quindi √® una richiesta
						for (byte n = 2; n < 6; n++)
							//temporaryButtonStatus[n - 2] = UDPPacketBuffer[n];
					//}
					//showSarabandaStatus();
					break;
				default:
					controller.print("Unkonwn command!!");
					break;
				}
			}
	*/

}


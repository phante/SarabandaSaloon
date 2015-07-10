/*
 Sarabanda Saloon Master
 autore: Elvis Del Tedesco
 data: 8/03/2014

 Libreria comune per la gestione del Sarabanda Saloon
 */

/**
 * Protocollo di rete
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

#ifndef SarabandaSaloon_h
#define SarabandaSaloon_h

#define DEBUG

#include <Arduino.h>
#include <Wire.h>                 // Comunicazione SPI
#include <LiquidCrystal_I2C.h>    // Libreria per cristalli liquidi
//#include <UIPEthernet.h>          // Libreria per ENC28J60
#include "EtherCard.h"         	 // Libreria per ENC28J60

#ifdef DEBUG
#include <IPAddress.h>
#endif

// Porte UDP per invio e ricezione
#define UDP_PORT 8888
// Dimensione del pacchetto UDP in byte
#define UDP_PACKET_MAX_SIZE 48
// Numero massimo di elementi nella rete
#define MAX_NET_NODE 5

// Informazioni per il debounce degli ingressi digitali
#define DEBOUNCE_DELAY 50
#define DEBOUCE_MAX_BUTTON 10

// Indirizzo broadcast di rete
const byte BROADCAST_IP[] = { 255, 255, 255, 255 };

#ifndef DEBUG_LCD_ROW
#define DEBUG_LCD_ROW 2
#endif

class SarabandaSaloon {
public:
	void begin(LiquidCrystal_I2C *lcd, bool isMaster = true);

	//void startEthernet(byte macAddress[], byte ipAddress[]); // UIPEthernet.h
	//int startUDPServer(EthernetUDP *UDPChannel); // UIPEthernet.h

	void setControllerPin(byte pinNumber, byte* pin);
	void runControllerPin();

	void setGamePin(byte* pin);
	void runGamePin();

	void setRelePin(byte* selected, byte* error, byte* optionOne, byte* optionTwo);

	void clear();
	void print(const char message[], byte row = 0, byte column = 0);

	void sendMasterStart();
	void sendSlaveRegistration();
	void sendSlaveRegistrationOK();
	void sendSlaveDeRegistration();

	void parseMessage(const char *message, unsigned int messageSize);

	void setMaster();
	void setSlave();
private:
	// Riferimento al display lcd inizializzato sul main
	LiquidCrystal_I2C *_lcd;

	// Server UDP per accettare comunicazioni in ingresso 
	//EthernetUDP _UDPChannel; // UIPEthernet.h

	// Contiene l'indirizzo degli altri elementi della rete
	// _netGraph[0] è il master
	//IPAddress _netGraph[MAX_NET_NODE]; // UIPEthernet.h

	// Gestione del debouce dei pulsanti di controllo
	byte _controllerPinNumber;
	byte *_controllerPin;
	byte *_controllerPinState;
	byte *_controllerPinLastState;
	long *_controllerPinLastDebounceTime;

	// Gestione del debouce dei pulsanti di gioco
	byte *_gamePin;
	byte _gamePinState[4] = {0};
	byte _gamePinLastState[4] = {0};
	long _gamePinLastDebounceTime[4] = {0};
	byte _gamePinEnabled;

	// Stato dei pulsanti del Sarabanda
	byte _gameStatus[4] = {0};
	byte _gamePreviousStatus[4] = {0};

	// Pin dei relè di uscita
	byte *_selectedRele;
	byte *_errorRele;
	byte *_optionOneRele;
	byte *_optionTwoRele;

	// Identifica se è attiva la modalità demo
	byte _demoIsActive;

	// Identifica se è master o slave
	bool _isMaster;

	// Header standard dei pacchetti del sarabanda saloon
	char messageHeader[11] = "SRBND-";

	char lastMessage[UDP_PACKET_MAX_SIZE] = "";
	long lastMessageMillis = 0;

	int controllerPinDebouce(int button);
	void onErrorButton();
	void onErrorMessage();
	void onError();

	void onFullResetButton();
	void onFullResetMessage();
	void onFullReset();

	void onResetButton();
	void onResetMessage();
	void onReset();

	void onDemoButton();
	void onDemoMessage();
	void onDemo();

	void onResetAll();

	void sendButtonStatus();
	void sendFullReset();
	void sendReset();
	void sendError();
	void sendDemo();
	void sendRESET();

	void receiveMaster(char message[]);
	void receiveSlave(char message[]);
	void receiveButtom(char message[]);
	void receiveFullReset(char message[]);
	void receiveReset(char message[]);
	void receiveError(char message[]);
	void receiveDemo(char message[]);

	char getButtonStatus(byte i);
	void setButtonStatus(byte i, char newStatus);
	void loadButtonStatus();

	void showButtonStatus();
	void showReleStatus();

	int gamePinDebouce(int button);
	int debounceDigital(int button, byte* pin, byte *pinState,
			byte *pinLastState, long *pinLastDebounceTime);

	void sendUDPMessage(const char outMessage[]);

	int isASarabandaSaloonMessage(const char *message);
	void trimHeaderFromMessage(char destination[], const char *message, unsigned int len);
};

#endif

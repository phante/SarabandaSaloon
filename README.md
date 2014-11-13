SarabandaSaloon
===============

SarabandaSaloon è il progetto di costruire un sistema di gioco simile al vecchio programma Sarabanda che andava in onda su Italia 1 qualche anno fa.

Il sistema è costituito da quattro colonne con un pulsante e luci, collegate ad un SarabandaSaloon Master, costuituito da un'Arduino Mega 2560 che si occupa di gestire la pressione esclusiva dei pulsanti e accensione delle luci. Il Master comunica con uno o più SarabandaSaloon Slave via ethernet (e quindi facilmente via wifi) sfruttando un semplice protocollo UDP.

I SarabandaSaloon Slave possono essere fisici, e quindi una pulsantiera sempre costituita da un'Arduino Uno collegato via Ethernet oppure anche software tramite un tool sviluppato in java sfruttanto JavaFX.


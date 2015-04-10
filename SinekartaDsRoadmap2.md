# Obiettivi #

## Obbiettivi primari ##
  * firma digitale con smartcard ed applet
  * firma digitale in formato CMS accettato dalla PA
  * firma digitale in formato PDF accettato dalla PA
  * firma digitale in formato XML accettato dalla PA
  * integrazione dei requisiti di sopra nella struttura generale dei webscript
  * installazione su alfresco

## Obbiettivi secondari ##
  * prevedere il supporto per la firma digitale via webservice
  * supportare  diverse disposizioni per i tipi di firma
  * supportare diverse disposizioni per il timestamp
  * rendere il core personalizzabile in termini di algoritmi e formati

## Linee guida generali sul risultato finale atteso ##
  * opzioni di default
    * produzione immediata e diretta di firme digitali riconosciute dalla PA locale
    * possibilità per gli sviluppatori di far fronte agevolmente a cambiamenti della normativa intervenendo sulle configurazioni
    * profilazione della PA locale: facoltà di switchare tra le impostazioni delle PA locali salvate
  * opzioni avanzate
    * indicare "real-time" il contesto di riferimento con cui dovrà esser compatibile la firma che si sta generando
    * possibilità per l'utente di salvare le impostazioni (es. accesso ai keystore), per recuperarle rapidamente in un secondo momento
  * parametri sulla firma
    * verifica della validità delle firme generate: verificate da Dike (e viceversa nella verifica)
    * la verifica dovrà indicare se una firma è generalmente valida ed avvisare con delle warning se non verrebbe riconosciuta dal contesto di riferimento (di default, PA)
    * public keyring: insieme di keystore locali o condivisi col server stesso da cui verranno recuperate le CA note (comprese quelle del keytool del jre) e tutti i certificati che, previa validazione della certification chain od esplicita accettazione dell'utente finale, riconosciuti da Sinekarta; delle warning notificheranno quando il certificato non risulta verificabile dal keyring attuale
  * altro
    * internazionalizzazione nativa di messaggi e formati
    * predisposizione per gli usi futuri (es. l'algoritmo ECDSA sarà supportato a partire da PDF 2.0 o in alcune Nazioni le chiavi RSA sono richieste a 2024 bit, ecc.; è bene che SineKarta ne tenga già conto)



# Situazione iniziale (29/08/2014) #

## Servizi di firma e marca ##

### Firma digitale in formato CMS ###
  * procedura di base (firma digitale EMBEDDED da keystore verificata da Dike) completamente operativa
  * struttura del core quasi definitiva, supporto di base a quasi tutte le linee guida di sopra
  * a quanto risulta dai test, BouncyCastle di default firma e verifica i CMSSignedData tramite rsaEncryption ("RSA/ECB/PKCS1Padding"), valido per Dike ma non sufficiente per verificare firme create con altri algoritmi
  * ogni volta che il service genera una firma vi applica tutte le verifiche note prima di restituirla
  * Dike verifica le firme di Sinekarta, Sinekarta verifica le firme di Dike
  * la disposizione DETACHED presenta dei problemi nella verifica su JUnit
  * algoritmi di digest, firma digitale e cipher sono codificati da opportune enum che agevolano il riconoscimento degli algoritmi usati
  * è già virtualmente possibile indicare diversi algoritmi di digest, l'implementazione con external sign tramite un provider proxato tuttavia al momento supporta solamente SHA256

### Firma digitale in formato PDF ###
  * diponibili gli esempi dalla precedente versione di SineKarta

### Firma digitale in formato XML ###
  * disponibili gli esempi estratti da EID

### TimeStamp ###
  * procedura di base (aggiunta di un timestamp generato da infocert e riconosciuto da Dike) completamente operativa
  * ottenere un formato equivalente a quello di Dike (teoricamente CAdES-T) ha richiesto un nuovo generico componente TimeStampedData per il riconoscimento e la generazione di specifici pattern ASN1, gestito da un service apposito
  * la disposizione del timestamp ENVELOPED come unsignedAttribute nelle firme digitali viene riconosciuta e verificata da Dike, ma visualizzata nell'interfaccia come fosse una seconda firma
  * JUnit test hanno prodotto marcature DETACHED (TSR) verificate con successo da Dike, ma tale funzionalità deve esser ancora integrata nel core
  * altre analisi sui patten ottenuti dalla marcatura apposta da Dike saranno necessari per identificare e riprodurre i formati TSD ed M7M

### Installazione su Alfresco ###
  * non è stato fatto ancora nulla, gestirla tramite maven ed amp
  * possibile problema dovuto alla presenza delle vecchie versioni di BC dentro Alfresco


## Client di firma digitale ##

### KeyStore ###
  * firma con certificato ricavato da keystore locale completamente operativa

### WebService ###
  * funzionalità da implementare, ma già supportata nel processo di firma

### SmartCard ###
  * firma digitale con SHA256 tramite SmartCard completamente operativa su JUnit
  * applet di firma pronta, ma ancora non è stato possibile testarla sul browser con successo
  * autodetect del driver tra le librerie di sistema
  * analisi e riconoscimento dei meccanismi supportati (e possibilità "virtuale" di applicare o meno un dato tipo di firma)
  * attualmente i pattern dei comandi iaik noti producono solo firme SHA256withRSA


## Architettura ed integrazione ##
  * sinekarta-ds: il progetto parent dichiara tutte le dipendenze ed i plugin necessari perché i rispettivi moduli li possano utilizzare
    * sinekarta-ds-commons: modello dati comune, utility e dipendenze comuni, usate in tutto il progetto
    * sinekarta-ds-core
    * sinekarta-ds-dto: protocolli per i dto che già tengono conto dell'internazionalizzazione dei formati, conversione a stringa delle entità model
    * sinekarta-ds-client: implementazione lato client dei provider di firma e della gestione smartcard
    * sinekarta-ds-alfresco
    * sinekarta-ds-share
    * sinekarta-ds-applet: jar dalle dipendenze minimali che include i sorgenti di cui ha bisogno, estratti dagli altri progetti, testato per girare su JDK1.5
  * usata la libreria bctsp\_jdk16\_146 di bouncycastle, facente a sua volta riferimento bcmail e bcprov per evitare che possano insorgere i precedenti problemi legati alla duplicazione dei package nei due jar
  * i progetti alfresco e share sono da aggiornare ai nuovi protocolli introdotti negli ultimi rilasci
  * la documentazione javadoc verrà applicata dopo che le varie problematiche saranno risolte e la struttura attuale si rivelerà stabile

## Interfaccia grafica ##
  * attualmente supporta il procedimento di base (keystore, DETACHED, timestamp)
  * grafica senza decorator
  * mancanza della scelta manuale del file keystore
  * la navigazione del wizard viene implementata con metodi javascript richiamati nel onload del body, in caso di step completeti con successo. Tale implementazione funziona ma è sicuramente da ristrutturare



# Situazione attesa (19/09/2014) #

## Servizi di firma e marca ##
  * funzionamento completo della firma XML almeno con disposition ENVELOPING
  * funzionamento completo della firma PDF
  * generazione delle marche nei formati a scelta dell'utente: TSR, TSD, M7M, CAdES-T

## Client di firma digitale ##
  * ottenere il funzionamento completo della smartcard via applet

## Interfaccia grafica ##
  * funzionamento completo di tutti i tipi di firma e timestamp tramite browser



# Roadmap #
|**OBIETTIVO**|**TEMPO STIMATO**|**SCADENZA STIMATA**|**NOTE**|**TEMPO EFFETTIVO**|**SCADENZA EFFETTIVA**|
|:------------|:----------------|:-------------------|:-------|:------------------|:---------------------|
|errori nel classpath con bouncycastle|? |??/??|utilizzo del solo bouncycastle-jdk16|8 |02/09|
|  -> itext scritto per bouncycastle-jdk15on|? |??/??|generato progetto itext-jdk16|? |??/??|
|funzionamento applet smartcard|16|02/09|in attesa del certificato|  | |
|  -> certificato di firma|8 |29/08|  | | |
|  -> esecuzione con successo di un'HelloWorld applet|8 |01/09|  | | |
|  -> esecuzione con successo dell'applet di firma|4 |02/09|  | | |
|  -> integrazione dell'applet di firma nei jsp|4 |02/09|  | | |
|funzionamento firma PDF|3 |05/09|anticipato in attesa di poter tornare sulla smartcard|  | |
|  -> JUnit a partire dal vecchio service in Sinekarta|4 |03/09|meccanismi deprecati, si cercano alternative|  | |
|  -> customizzazione formati, algoritmi ecc.|4 |03/09|  | | |
|  -> JUnit sul funzionamento webscript firma PDF lato server|4 |04/09|  | | |
|  -> introduzione della signature disposition nelle opzioni utente, refactoring|8 |05/09|  | | |
|  -> funzionamento completo firma PDF tramite browser|4 |05/09|  | | |
|funzionamento firma XML|32|11/09|  | | |
|  -> analisi e studio esempi EID|8 |08/09|  | | |
|  -> JUnit per produrre e verificare firme XML accettate dalla PA|4 |09/09|  | | |
|  -> sviluppo altre disposition, se fattibile in tempi brevi|4 |09/09|  | | |
|  -> JUnit sul funzionamento webscript firma XML lato server|8 |10/09|  | | |
|  -> funzionamento completo firma XML tramite browser|8 |11/09|  | | |
|personalizzazione timestamp|32|17/09|  | | |
|  -> analisi pattern ASN1 prodotti da Dike|8 |12/09|  | | |
|  -> JUnit sulla produzione marche nei vari formati|8 |15/09|  | | |
|  -> JUnit sul funzionamento webscript marche lato server|8 |16/09|  | | |
|  -> funzionamento completo marche tramite browser|8 |17/09|  | | |
|installazione su alfresco|16|19/09|  | | |
|  -> preparare gli amp durante i test dei lati share / alfresco|16|19/09|  | | |



# Problematiche riscontrate #

## Errori nel classpath di bouncycastle ##
Test su sinekarta-ds-core generano errori runtime quando la JRE tenta utilizzare alcune classi di BouncyCastle. Dalle verifiche è emerso che:
  * bouncycastle non è retrocompatibile e la coesistenza di jar di versioni diverse all'interno del classpath genererà errori runtime; il bugfix prevede di rimuovere dai pom ogni riferimento a versioni di bouncycastle diverse dalla _-jdk16_.
  * bcprov-jdk16 e bcmail-jdk16 presentano duplicazioni nei package e nelle classi cui afferiscono diversi protocolli, il che induce la JRE a sbagliare riferimenti; bctsp-jdk16 integra entrambe le dipendenze in maniera consistente
  * a seconda che si desideri minimizzare l'utilizzo delle dipendenze nel progetto, è necessario includere nel pom alternativamente bctsp-jdk16 oppure bcprov-jdk16
  * itext è stato scritto per supportare bouncycastle-jdk15on, perché funzioni nel nuovo ambiente sarà necessario patcharlo
  * la stessa problematica si ripeterà nell'integrazione di SineKarta con Alfresco, in quanto anche esso si riferisce a bouncycastle-jdk15on

|**PROBLEMATICA**|**TEMPO**|**DATA FIX**|**INTERVENTO**|**ESITO**|
|:---------------|:--------|:-----------|:-------------|:--------|
|collisioni tra bcprov e bcmail|4 |29/08|analisi e correzione dipendenze|utilizzo alternativo di bcprov o bctsp|
|collisioni tra diverse versioni di bouncycastle|8 |01/09|analisi e correzione dipendenze|rimozione di ogni riferimento a versioni diverse dalla _-jdk16_|

## Introduzione della ExternalSignature per la firma PDF ##
La procedura di fima PDF nella precedente versione di SineKarta si basa su una patch di PDFStamper che consente di introdurvi una firma digitale generata esternamente. Le ultime versioni di _itext_ forniscono due nuove interfacce  ExternalDigest ed ExternalSignature che supportano nativamente la firma esterna, l'intento sarà pertanto quello di sfruttarle per ottenere lo stesso risultato senza alterare la normale logica del framework.
Applicare una patch a _itext_ si rivela però necessario in quanto questo è stato strutturato per lavorare con bouncycastle-jdk15on e la jdk15. Il progetto itext-jdk16 ora integrato in sinekarta è stato ottenuto le modifiche necessarie ad adattare il sorgente originale di itext al nuovo ambiente operativo.

|**PROBLEMATICA**|**TEMPO**|**DATA FIX**|**INTERVENTO**|**ESITO**|
|:---------------|:--------|:-----------|:-------------|:--------|
|firma PDF ottenuta patchando il PDFStamper|? |??/??|ricerca su [itext](http://itextpdf.com/book/digitalsignatures20130304.pdf)|utilizzo di ExternalDigest ed ExternalSignature con iText 5.5.2|
|iText non compatibile con bouncycastle-jdk16|? |??/??|sviluppo di una patch|itext-jdk16|

## Duplicazione di bouncycastle in Alfresco ##
L'installazione di Alfresco contiene già una versione di bouncycastle-jdk15on ed è fatto noto che la coesistenza di due versioni differenti di bouncycastle causerà problematiche di classpath. Il processo di installazione di SineKarta dovrà esser in grado di rimuovere da Alfresco le librerie datate. Tale operazione dovrà esser automaticamente compatibile con ciascuna installazione di Alfresco.

|**PROBLEMATICA**|**TEMPO**|**DATA FIX**|**INTERVENTO**|**ESITO**|
|:---------------|:--------|:-----------|:-------------|:--------|
|duplicazione di bouncycastle nel classpath di Alfresco|X |XX/XX|aggiunta del controllo all'amp|  |
|retrocompatibilità patch|X |XX/XX|  | |



# Memorandum #
Questa sezione di brainstorming raccoglierà informazioni e spunti interessanti scoperti durante la soluzione dei problemi che non si ha potuto approfondire da subito ma su cui val la pena di tornare in un secondo momento
  * quando possibile studiare approfonditamente la [guida](http://itextpdf.com/book/digitalsignatures20130304.pdf) di Bruno Lowagie
  * prendere spunto da _com.itextpdf.text.pdf.security.BouncyCastleDigest_ per rimpiazzare l'obbrobrioso _ProxySecurityProvider_ usato per l'external signature del core
  * l'idea di _ExternalDigest_ ed _ExternalSignature_ può esser applicata sia per rifare il provider di firma del core che quello usato dal client; potrebbero esser legati direttamente tramite composizione
  * utilizzare in seguito _CrlClient_ e _OcspClient_ per il controllo sulla sospensione e la validità dei certificati, usare simili accorgimenti anche per gli altri tipi di firma
  * riportare parte della sua struttura di _TSAClientBouncyCastle_ sui service di SineKarta ed eventualmente integrarlo al posto di _TimeStampUtils_
  * _NOTE: If you need to sign a PDF/A file, you should use the createSignature() method available in the PdfAStamper stamper class. This class can be found in a separate itext-pdfa.jar starting with iText 5.3.4._
  * _We can use Bouncy Castle as security provider for the digest by choosing an instance of the BouncyCastleDigest class. If you want another provider, use the ProviderDigest class._
  * l'attuale implementazione di RemoteDigest si basa su BouncyCastle, si potrebbe personalizzare invece _ProviderDigest(conf.getProviderName())_ per renderle configurabile al 100%
  * quando possibile studiare l'interessantissimo articolo [jscep](https://github.com/jscep/jscep/blob/master/README.md) su github.com
  * studiare Formatter per re-implementare l'internazionalizzazione
# myapplicationpaypalsdk - Aplication android for paypal transactions

This is an example of an android app using Braintree SDK (JAVA) for PayPal transactions.

## Used tools

For this example, I use the Braintree SDK version 3.6.0 (java, Client-Server), and volley SDK version 1.1.0. 
To test the app, I use Bluestacks (Android simulator for PC, it works under Android 4.1 or +) and XAMPP for the server side. 

## Setup Instructions for testing using BlueStacks and XAMPP

1. Import the project
2. Two options, 1) configure IP address for the PC to 192.168.1.49, or 2) Configure in the MainActivity.java file the IP address of your PC
3. Generate APK
4. Copy APK to android device (For testing, I'm using BlueStacks. Then, I copied APK file to BlueStacks)
5. To configure server side : 
	5.1 Download and install XAMPP on the PC
	5.2 Once installed, copy the BraintreePayments folder inside the xampp\htdocs
	5.3 In XAMPP control panel, start Apache server
	5.4 Strt BlueStacks and test the app
6. Successfull payments can be checked in SandBox accounts
								
## SandBox paypal account used as "merchant"

Environment('sandbox');
MerchantId('59frcs4dbb962mnx');
publicKey('x3rydvts9b9njfhs');
privateKey('b67c6c4ea695d3771b2909c359ad59c8');

This info is configured (and can be changed) in file xampp\htdocs\BraintreePayments\braintree_php\braintree_init.php

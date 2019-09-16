import 'dart:convert';

import 'package:bfnwebflutter/data/account.dart';
import 'package:bfnwebflutter/net.dart';
import 'package:flutter/material.dart';

import 'data/invoice.dart';

void main() => runApp(MyApp());

class MyApp extends StatelessWidget {
  // This widget is the root of your application.
  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Flutter Demo',
      theme: ThemeData(
        primarySwatch: Colors.blue,
      ),
      home: MyHomePage(title: 'BFN - Business Finance Network 2019'),
    );
  }
}

class MyHomePage extends StatefulWidget {
  MyHomePage({Key key, this.title}) : super(key: key);

  final String title;

  @override
  _MyHomePageState createState() => _MyHomePageState();
}

class _MyHomePageState extends State<MyHomePage> {
  int _counter = 0;
  List<AccountInfo> accounts = List();
  List<Invoice> invoices = List();

  void _incrementCounter() {
    setState(() {
      _counter++;
    });
    _getNet();
  }

  _getNet() async {
    var ping = await Net.ping();
    print(ping);

    String result = await Net.getAccounts();
    List map = json.decode(result);
    print('🏈  🏈  🏈  🏈  about to print accounts received from corda ...  🏈  🏈  🏈  🏈 ');
    map.forEach((f) {
      accounts.add(AccountInfo.fromJson(f));
    });
    print(
        '🧩 🧩 🧩 🧩 🧩 🧩 🧩  getAccounts found  💜 ${accounts.length}  💜 accounts on corda node  🧩 🧩 🧩 🧩');
    accounts.forEach((acc) {
      print('🧩 🧩 account: ' + acc.toJson().toString() + " 🧩 ");
    });
    print('🏈  🏈  🏈  🏈  completed printing accounts...  🏈  🏈  🏈  🏈 ');

    String result1 = await Net.getInvoices();
    List map1 = json.decode(result1);
    print('\n\n🏈  🏈  🏈  🏈  about to print invoices received from corda ...  🏈  🏈  🏈  🏈 ');
    map1.forEach((f) {
      invoices.add(Invoice.fromJson(f));
    });
    print(
        '🍎 🍎 🍎 🍎 🍎 🍎 🍎   getInvoices found ${invoices.length} invoices on corda node  🍎 🍎 🍎 🍎 ');
    invoices.forEach((acc) {
      print('🍎 🍎 invoice: ' + acc.toJson().toString() + " 🍎 ");
    });
    print('🏈  🏈  🏈  🏈  completed printing invoices...  🏈  🏈  🏈  🏈 ');
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text(widget.title),
      ),
      body: Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: <Widget>[
            Text(
              'You have pushed the button this many times:',
            ),
            Text(
              '$_counter',
              style: Theme.of(context).textTheme.display1,
            ),
          ],
        ),
      ),
      floatingActionButton: FloatingActionButton(
        onPressed: _incrementCounter,
        tooltip: 'Increment',
        child: Icon(Icons.add),
      ), // This trailing comma makes auto-formatting nicer for build methods.
    );
  }
}

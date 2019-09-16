import 'package:http/http.dart' as http;
class Net {

  static Future getNetwork() async {

    Future<http.Response> fetchPost() {
      return http.get('https://localhost:10411/ping');
    }
  }
}

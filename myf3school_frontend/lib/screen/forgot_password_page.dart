import 'package:flutter/material.dart';


class ForgotPasswordPage extends StatelessWidget {
  late var phone_number = new TextEditingController();
  @override
  Widget build(BuildContext context) {
    
    return Scaffold(
      body: Padding(
        padding: const EdgeInsets.all(20.0),
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            Column(
              mainAxisAlignment: MainAxisAlignment.center,
              children: [
                Image.network(width: 200,'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcTCPedHYgEbswX5-OREJD1TKplrG2j_eyOqag&s')
              ],
            ),
            SizedBox(
              height: 30,
            ),
            Row(
              children: [
                Text('Xác minh số điện thoại',style: TextStyle(fontWeight: FontWeight.w500, fontSize: 20),),
              ]
            ),

            Text('Mã xác thực sẽ được gửi đến số này để bạn đặt lại mật khẩu'),
            SizedBox(height: 10,),
            TextField(
              controller: phone_number,
              decoration: InputDecoration(
                hintText: 'Phone Number',
                border: OutlineInputBorder(
                  borderRadius: BorderRadius.circular(12),
                )
              ),
            ),
            SizedBox(height: 15,),
            SizedBox(
              width: double.infinity,
              height: 46,
              child: ElevatedButton(
                style: ElevatedButton.styleFrom(
                  elevation: 0,
                  backgroundColor: Colors.amber,
                  shape: RoundedRectangleBorder(
                    borderRadius: BorderRadius.circular(8),
                  )
                ),
                  onPressed: (){

              }, child:
              Text('Gửi mã OTP',style: TextStyle(
                color: Colors.white
              ),)
              ),
            )
          ],
        ),
      ),
    );
  }

}
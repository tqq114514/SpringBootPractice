package com.tedu.springbootpractice.controller;

import com.tedu.springbootpractice.entity.User;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;

/*当前类处理与用户相关的业务操作*/
@Controller
public class UserController {
    private static File userDir;
    static{
        userDir = new File("./Users");
        if (!userDir.exists()){
            userDir.mkdirs();
        }
    }
    @RequestMapping( "/regUser")
    //请求路径映射
    public void reg(HttpServletRequest request, HttpServletResponse response) {
        /*request获取浏览器发给我们的信息*/
        /*response返回给用户信息*/
        System.out.print("开始处理用户注册流程");
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        String nickname = request.getParameter("nickname");
        String ageStr = request.getParameter("age");
        System.out.print(username + "," + password + "," + nickname + "," + ageStr);
        /*验证输入信息是否合法*/
        /*null：防止前端用户界面没有阻止的不合法请求，比如提交url时不写其中一个参数，导致没有取到数据引起的空指针异常*/
        if (username == null || "".equals(username) || password == null || password.isEmpty() ||
                nickname == null || nickname.isEmpty() ||
                ageStr == null || ageStr.isEmpty() ||
                !ageStr.matches("[0-9]+")) {
            /*输入内容不合法，跳转错误页面*/
            try {
                response.sendRedirect("/reg_info_error.html");
            } catch (IOException e) {
                e.printStackTrace();
            }
            return;
        }
        //验证通过后保存用户输入信息
        int age = Integer.parseInt(ageStr);
        User user = new User(username, password, nickname, age);
        File file = new File(userDir, username + ".obj");

        /*用户已存在(文件已存在)，就重新注册*/
        if (file.exists()) {
            try {
                response.sendRedirect("./have_user.html");
            } catch (IOException e) {
                e.printStackTrace();
            }
            return;
        }

        try (FileOutputStream fos = new FileOutputStream(file);  //文件输出流，是字节流，用于向文件中写字节
             ObjectOutputStream oos = new ObjectOutputStream(fos);  //对象输出流，将Java对象转换为字节流，与便于写入到文件中
        ) {
            oos.writeObject(user);
            response.sendRedirect("./reg_success.html");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @RequestMapping("/loginUser")
    public void login(HttpServletRequest request, HttpServletResponse response)  {
        System.out.print("开始处理用户登录流程");
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        if (username == null || "".equals(username) || password == null || password.isEmpty() ) {
            /*输入内容不合法，跳转错误页面*/
            try {
                response.sendRedirect("/login_info_error.html");
            } catch (IOException e) {
                e.printStackTrace();
            }
            return;
        }
        /*用当前登录用户的用户名拼接成一个待对比的文件名，去Users下找他的注册信息*/
        File file = new File(userDir,username+".obj");
        if(file.exists()){  //文件存在就说明用户注册过
            /*反序列化该文件得到该用户的注册信息*/
            try {
                FileInputStream fis = new FileInputStream(file);
                ObjectInputStream ois = new ObjectInputStream(fis);
                User user = (User)ois.readObject();
                /*只有用户名存在文件中才会对密码进行对比，所以只需要对比密码是否正确就可以了*/
                if(user.getPassword().equals(password)){
                    response.sendRedirect("./login_success.html");
                }else {
                    response.sendRedirect("./login_fail.html");
                }
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }else {  //文件不存在则说明不是一个有效的注册用户
            try {
                response.sendRedirect("./login_fail.html");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }



    }

}

package com.tedu.springbootpractice.controller;

import com.tedu.springbootpractice.entity.User;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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

        /*用户已存在(文件名已存在)，就重新注册*/
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

    @RequestMapping("/userList")
    public void userList(HttpServletRequest request,HttpServletResponse response){
        System.out.println("开始处理显示用户列表");
        /*获取users目录里的所有文件，存入一个集合备用*/
        List<User> userList = new ArrayList<>();
        /*获取users目录下的所有obj文件,保存到file[]数组中*/
        File[] subs = userDir.listFiles(users->users.getName().endsWith(".obj"));
        /*File[] subs = userDir.listFiles(new FileFilter() {
            @Override
            public boolean accept(File Users) {  //传入文件名
                return Users.getName().endsWith(".obj");
            }
        });
*/
        /*从file[]里挨个读取文件名，边读边保存到集合中*/
        for (File sub:subs){
            try(FileInputStream fis = new FileInputStream(sub);
                ObjectInputStream ois = new ObjectInputStream(fis);
                ) {
                User user = (User) ois.readObject();
                userList.add(user);
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }

        }


        response.setContentType("text/html;charset=utf-8");
        try {
            PrintWriter pw = response.getWriter();
            pw.println("<!DOCTYPE html>");
            pw.println("<html lang=\"zh-cn\">");
            pw.println("<head>");
            pw.println("<meta charset=\"UTF-8\">");
            pw.println("<title>用户列表</title>");
            pw.println("</head>");
            pw.println("<body>");
            pw.println("<center>");
            pw.println("<h2>用户列表</h2>");
            pw.println("<table border=\"1px\">");
            pw.println("<tr>");
            pw.println("<td>用户名:</td>");
            pw.println("<td>密码:</td>");
            pw.println("<td>昵称:</td>");
            pw.println("<td>年龄:</td>");
            pw.println("<tr>");


            /*遍历集合，通过引用对象打点访问get方法获取私有属性*/
            for (User user:userList){
                pw.println("<tr>");
                pw.println("<td>"+user.getUsername()+"</td>");
                pw.println("<td>"+user.getPassword()+"</td>");
                pw.println("<td>"+user.getNickname()+"</td>");
                pw.println("<td>"+user.getAge()+"</td>");
                pw.println("<tr>");
            }

            pw.println("</table>");
            pw.println("</center>");
            pw.println("</body>");
            pw.println("</html>");
        } catch (IOException e) {
            e.printStackTrace();
        }



    }

}

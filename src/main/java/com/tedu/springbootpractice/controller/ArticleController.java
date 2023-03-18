package com.tedu.springbootpractice.controller;

import com.tedu.springbootpractice.entity.Article;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;

/*声明当前类处理发表文件的操作*/
@Controller
public class ArticleController {
    /*由于保存文章内容的目录只需要创建一次，则我们将其置于静态块
    * 利用FiLe创建文件目录*/
   public static File writerDir;
   static {
       writerDir = new File("./writerUsers");
       if (!writerDir.exists()){
           writerDir.mkdirs();
           /*mkdirs()方法创建该文件夹及其所有缺少的父文件夹*/
       }
   }


    @RequestMapping("/writeArticle")
    public void writeArticle(HttpServletRequest request , HttpServletResponse response){
        String title = request.getParameter("title");
        String author = request.getParameter("author");
        String content = request.getParameter("content");

        /*判断验证信息是否合法，并阻止前端的非法请求
        * 包括用户在url上随意提交不带完整参数产生的null可能引发的空指针异常
        * 表单没有写内容产生的空字符串*/
        if(title == null || title.isEmpty()||
        author == null || "".equals(author)||
        content == null || content.isEmpty()){
            try {
                response.sendRedirect("/article_fail.html");
            } catch (IOException e) {
                e.printStackTrace();
            }
            return;  //信息不合法，则直接结束
        }
        System.out.printf(title+author+content);

        File file = new File(writerDir,title+".obj");  //创建的文件父目录为writerDir,文件名为标题.obj
        Article article = new Article(title,author,content);

        /*文章已存在(文件已存在)，就重新注册*/
        if (file.exists()) {
            try {
                response.sendRedirect("./article_fail.html");
            } catch (IOException e) {
                e.printStackTrace();
            }
            return;
        }

        try(FileOutputStream fos = new FileOutputStream(file);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            ) {
            oos.writeObject(article);
            response.sendRedirect("/article_success.html");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

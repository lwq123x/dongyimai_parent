package com.offcn.shop.controller;

import com.offcn.entity.Result;
import com.offcn.utils.FastDFSClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.swing.plaf.PanelUI;

/**
 * 文件上传控制器
 */
@RestController
public class UploadController {

    //读取属性文件的value值
    @Value("${FILE_SERVER_URL}")
    private String FILE_SERVER_URL; //文件服务器地址

    @RequestMapping("/upload")
    public Result upload(MultipartFile file) {
        //1.获取上传文件的名称
        String filename = file.getOriginalFilename();
        //2.根据名称截取扩展名
        String extName = filename.substring(filename.lastIndexOf(".") + 1);
        //3.使用文件上传的工具类执行上传操作
        try {
            //创建一个FastDFS的客户端
            FastDFSClient fastDFSClient = new FastDFSClient("classpath:config/fdfs_client.conf");
            //4.返回上传文件的存储路径
            String path = fastDFSClient.uploadFile(file.getBytes(), extName);
            //拼接返回的ip地址和返回的url, 拼装成完整的url
            String url = FILE_SERVER_URL + path;
            //利用Result 返回url加载图片
            return new Result(true, url);
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, "上传失败!");
        }

    }
}

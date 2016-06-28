package cn.zhoujia.concealdemo;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.android.crypto.keychain.SharedPrefsBackedKeyChain;
import com.facebook.crypto.Crypto;
import com.facebook.crypto.Entity;
import com.facebook.crypto.exception.CryptoInitializationException;
import com.facebook.crypto.exception.KeyChainException;
import com.facebook.crypto.util.SystemNativeCryptoLibrary;

import org.apache.http.util.EncodingUtils;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {

    Crypto crypto;
    Entity entity;

    Activity activity = MainActivity.this;
    @Bind(R.id.txt_str)
    TextView txtStr;
    @Bind(R.id.btn_encrypt_str)
    Button btnEncryptStr;
    @Bind(R.id.btn_decode_str)
    Button btnDecodeStr;
    @Bind(R.id.btn_encrypt_file)
    Button btnEncryptFile;
    @Bind(R.id.btn_decode_file)
    Button btnDecodeFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        //使用秘钥链和原生库的默认实现，来创建一个新的加密对象
        crypto = new Crypto(
                new SharedPrefsBackedKeyChain(activity),
                new SystemNativeCryptoLibrary());

        entity = new Entity("text");

    }

    @OnClick({R.id.btn_encrypt_str, R.id.btn_decode_str, R.id.btn_encrypt_file, R.id.btn_decode_file})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_encrypt_str:
                //加密字符串
                byte[] cipherText = new byte[0];
                try {
                    cipherText = crypto.encrypt("我随便在这里输了一点文字".getBytes(), entity);
                } catch (KeyChainException e) {
                    e.printStackTrace();
                } catch (CryptoInitializationException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                txtStr.setText(new String(cipherText));
                break;
            case R.id.btn_decode_str:
                // 解密字符串
                byte[] plainText = new byte[0];
                try {
                    plainText = crypto.decrypt(crypto.encrypt("我随便在这里输了一点文字".getBytes(), entity), entity);
                } catch (KeyChainException e) {
                    e.printStackTrace();
                } catch (CryptoInitializationException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                txtStr.setText(new String(plainText));
                break;
            case R.id.btn_encrypt_file:
                encryption(activity);
                break;
            case R.id.btn_decode_file:
                decryption(activity);
                break;
        }
    }


    //加密
    private void encryption(Context context) {
        //检查加密功能是否可用
        //如果Android没有正确载入库，则此步骤可能失败
        if (!crypto.isAvailable()) {
            Toast.makeText(context, "ENCRYPTION FAIL!", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            //加密后的文件路径
            File file = new File(Environment.getExternalStorageDirectory() + "/text.txt");
            if (!file.exists()) {
                file.createNewFile();
            }

            //获取源文件内容
            FileInputStream fin = new FileInputStream(file);
            int length = fin.available();
            byte[] buffer = new byte[length];
            fin.read(buffer);
            String res = EncodingUtils.getString(buffer, "UTF-8");
            fin.close();
            Log.e("res", res);


            //加密文件
            OutputStream fileStream = new BufferedOutputStream(new FileOutputStream(file));
            OutputStream outputStream = crypto.getCipherOutputStream(fileStream, entity);
            //写入内容
            outputStream.write(res.getBytes());
            outputStream.close();

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (CryptoInitializationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (KeyChainException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private void decryption(Context context) {
        try {
            //需要解密的文件路径
            File file = new File(Environment.getExternalStorageDirectory() + "/text.txt");
            FileInputStream fileStream = new FileInputStream(file);

            //解密
            InputStream inputStream;
            inputStream = crypto.getCipherInputStream(fileStream, entity);
            int read;
            byte[] buffer = new byte[1024];

            //解密后的文本
            String plainString = new String();
            StringBuilder stringBuilder = new StringBuilder();
            while ((read = inputStream.read(buffer)) > 0) {
                stringBuilder.append(new String(buffer, 0, read));
            }
            plainString = stringBuilder.toString();
            Toast.makeText(context, plainString, Toast.LENGTH_SHORT).show();

            //写入解密后的内容
            FileOutputStream fout = new FileOutputStream(file);
            byte[] bytes = plainString.getBytes();
            fout.write(bytes);
            fout.close();

            inputStream.close();


        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (CryptoInitializationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (KeyChainException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }


}

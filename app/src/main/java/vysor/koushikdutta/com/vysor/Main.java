package vysor.koushikdutta.com.vysor;

import android.graphics.Bitmap;
import android.graphics.Point;
import android.os.Build;
import android.os.Looper;
import android.util.Base64;

import com.koushikdutta.async.AsyncServer;
import com.koushikdutta.async.http.WebSocket;
import com.koushikdutta.async.http.server.AsyncHttpServer;
import com.koushikdutta.async.http.server.AsyncHttpServerRequest;
import com.koushikdutta.async.http.server.AsyncHttpServerResponse;
import com.koushikdutta.async.http.server.HttpServerRequestCallback;
import com.koushikdutta.virtualdisplay.SurfaceControlVirtualDisplayFactory;

import java.io.ByteArrayOutputStream;
/**
 * Created by zhaofei on 16/12/31.
 */
public class Main {

    static Looper looper;

    public static void main(String[] args) throws Exception {
        AsyncHttpServer httpServer = new AsyncHttpServer() {
            protected boolean onRequest(AsyncHttpServerRequest request, AsyncHttpServerResponse response) {
                return super.onRequest(request, response);
            }
        };

        Looper.prepare();
        looper = Looper.myLooper();
        System.out.println("Andcast Main Entry!");
        AsyncServer server = new AsyncServer();
        //截图
        httpServer.get("/screenshot.jpg", new HttpServerRequestCallback() {
            public void onRequest(AsyncHttpServerRequest request, AsyncHttpServerResponse response) {
                try {
                    Bitmap bitmap = screenshot();
                    ByteArrayOutputStream bout = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 60, bout);
                    bout.flush();
                    response.send("image/jpeg", bout.toByteArray());
                    return;
                } catch (Exception e) {
                    response.code(500);
                    response.send(e.toString());
                    return;
                }
            }
        });
        httpServer.websocket("/socket", new AsyncHttpServer.WebSocketRequestCallback() {
            @Override
            public void onConnected(final WebSocket webSocket, AsyncHttpServerRequest asyncHttpServerRequest) {
                webSocket.setStringCallback(new WebSocket.StringCallback() {
                    @Override
                    public void onStringAvailable(String s) {
                        if("1".equals(s.trim())){
                            try{
                                Bitmap bitmap = screenshot();
                                ByteArrayOutputStream bout = new ByteArrayOutputStream();
                                bitmap.compress(Bitmap.CompressFormat.JPEG, 30, bout);
                                bout.flush();
                                webSocket.send(Base64.encodeToString(bout.toByteArray(), Base64.DEFAULT));
                            }catch (Exception ex){
                                webSocket.send("");
                            }
                        } else{
                            webSocket.send("you says :"+s);
                        }
                    }
                });
            }
        });
        httpServer.listen(server, 53516);

        Looper.loop();
    }


    public static Bitmap screenshot() throws Exception {
        String surfaceClassName;
        Point size = SurfaceControlVirtualDisplayFactory.getCurrentDisplaySize(false);
        if (Build.VERSION.SDK_INT <= 17) {
            surfaceClassName = "android.view.Surface";
        } else {
            surfaceClassName = "android.view.SurfaceControl";
        }
        Bitmap b = (Bitmap) Class.forName(surfaceClassName).getDeclaredMethod("screenshot", new Class[]{Integer.TYPE, Integer.TYPE}).invoke(null, new Object[]{Integer.valueOf(size.x), Integer.valueOf(size.y)});
        return b;
    }
}
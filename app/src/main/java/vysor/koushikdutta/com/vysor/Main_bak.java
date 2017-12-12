//package vysor.koushikdutta.com.vysor;
//
//import android.content.ClipData;
//import android.content.IOnPrimaryClipChangedListener;
//import android.graphics.Bitmap;
//import android.graphics.Point;
//import android.hardware.input.InputManager;
//import android.os.Build;
//import android.os.IBinder;
//import android.os.IPowerManager;
//import android.os.Looper;
//import android.os.RemoteException;
//import android.os.SystemClock;
//import android.util.Log;
//import android.view.IRotationWatcher;
//import android.view.IWindowManager;
//import android.view.InputEvent;
//import android.view.KeyCharacterMap;
//import android.view.KeyEvent;
//import android.view.MotionEvent;
//
//import com.koushikdutta.async.AsyncServer;
//import com.koushikdutta.async.AsyncServerSocket;
//import com.koushikdutta.async.AsyncSocket;
//import com.koushikdutta.async.BufferedDataSink;
//import com.koushikdutta.async.ByteBufferList;
//import com.koushikdutta.async.DataSink;
//import com.koushikdutta.async.callback.CompletedCallback;
//import com.koushikdutta.async.callback.DataCallback;
//import com.koushikdutta.async.callback.ListenCallback;
//import com.koushikdutta.async.http.WebSocket;
//import com.koushikdutta.async.http.server.AsyncHttpServer;
//import com.koushikdutta.async.http.server.AsyncHttpServerRequest;
//import com.koushikdutta.async.http.server.AsyncHttpServerResponse;
//import com.koushikdutta.async.http.server.HttpServerRequestCallback;
//import com.koushikdutta.virtualdisplay.StdOutDevice;
//import com.koushikdutta.virtualdisplay.SurfaceControlVirtualDisplayFactory;
//import com.koushikdutta.vysor.IClipboard;
//
//import org.json.JSONException;
//import org.json.JSONObject;
//
//import java.io.ByteArrayOutputStream;
//import java.lang.reflect.InvocationTargetException;
//import java.lang.reflect.Method;
//import java.nio.ByteBuffer;
//import java.nio.ByteOrder;
//
///**
// * Created by zhaofei on 16/12/31.
// */
//public class Main_bak {
//
//    private static final String LOGTAG = "VysorMain";
//    static AsyncServer server = new AsyncServer();
//    static DataSink webSocket;
//    static Object activityManager;
//    static Method broadcastIntent;
//    static boolean isImeRunning;
//    static Looper looper;
//    static StdOutDevice current;
//    static double resolution = 0.0D;
//
//    public static void main(String[] args) throws Exception {
//        Looper.prepare();
//        looper = Looper.myLooper();
//        System.out.println("Andcast Main Entry!");
//        AsyncServer server = new AsyncServer();
//        AsyncHttpServer httpServer = new AsyncHttpServer() {
//            protected boolean onRequest(AsyncHttpServerRequest request, AsyncHttpServerResponse response) {
//                Log.i(Main_bak.LOGTAG, request.getHeaders().toString());
//                return super.onRequest(request, response);
//            }
//        };
//
//        final InputManager localObject1 = (InputManager)InputManager.class.getDeclaredMethod("getInstance", new Class[0]).invoke(null, new Object[0]);
//        MotionEvent.class.getDeclaredMethod("obtain", new Class[0]).setAccessible(true);
//        final Method localObject2 = InputManager.class.getMethod("injectInputEvent", new Class[]{InputEvent.class, Integer.TYPE});
//        KeyCharacterMap localObject3 = KeyCharacterMap.load(-1);
//        Method localObject5 = Class.forName("android.os.ServiceManager").getDeclaredMethod("getService", new Class[]{String.class});
//        final IClipboard localObject4 = IClipboard.Stub.asInterface((IBinder) ((Method) localObject5).invoke(null, new Object[]{"clipboard"}));
//        IOnPrimaryClipChangedListener localObject6 = new IOnPrimaryClipChangedListener.Stub()
//        {
//            public void dispatchPrimaryClipChanged()
//                    throws RemoteException
//            {
//                if (Main_bak.webSocket == null) {
//                    return;
//                }
//                try
//                {
//                    ClipData localClipData = localObject4.getPrimaryClip("com.android.shell");
//                    JSONObject localJSONObject = new JSONObject();
//                    localJSONObject.put("type", "clip");
//                    localJSONObject.put("clip", localClipData.getItemAt(0).getText());
//                    Main_bak.sendEvent(localJSONObject);
//                    return;
//                }
//                catch (Exception localException)
//                {
//                    Log.e("VysorMain", "Clip error", localException);
//                }
//            }
//        };
//        if (localObject4 != null) {
//            ((IClipboard)localObject4).addPrimaryClipChangedListener((IOnPrimaryClipChangedListener)localObject6, null);
//        }
//        final IPowerManager localObject41 = IPowerManager.Stub.asInterface((IBinder)((Method)localObject5).invoke(null, new Object[] { "power" }));
//        final IWindowManager localObject51 = IWindowManager.Stub.asInterface((IBinder)((Method)localObject5).invoke(null, new Object[] { "window" }));
//        activityManager = Class.forName("android.app.ActivityManagerNative").getDeclaredMethod("getDefault", new Class[0]).invoke(null, new Object[0]);
//        Method[] localObject61 = activityManager.getClass().getDeclaredMethods();
//        int j = localObject61.length;
//        int i = 0;
//
//
//        AsyncServerSocket localAsyncServerSocket;
//        for(Method method : localObject61){
//            if (!method.getName().equals("broadcastIntent")) {
//                break;
//            }
//        }
//
//
//        ((IWindowManager)localObject51).watchRotation(new IRotationWatcher.Stub()
//        {
//            public void onRotationChanged(int paramAnonymousInt)
//                    throws RemoteException
//            {
//                if (Main_bak.webSocket == null) {
//                    return;
//                }
//                Main_bak.sendDisplayInfo();
//            }
//        });
//        CompletedCallback localObject611 = new CompletedCallback()
//        {
//            public void onCompleted(Exception paramAnonymousException)
//            {
//                Log.i("VysorMain", "Websocket closed...");
//                Main_bak.looper.quit();
//            }
//        };
//
//        //截图
//        httpServer.get("/screenshot.jpg", new HttpServerRequestCallback() {
//            public void onRequest(AsyncHttpServerRequest request, AsyncHttpServerResponse response) {
//                try {
//                    Bitmap bitmap = screenshot();
//                    ByteArrayOutputStream bout = new ByteArrayOutputStream();
//                    bitmap.compress(Bitmap.CompressFormat.JPEG, 50, bout);
//                    bout.flush();
//                    response.send("image/jpeg",bout.toByteArray());
//                    return;
//                } catch (Exception e) {
//                    response.code(500);
//                    response.send(e.toString());
//                    return;
//                }
//            }
//        });
//
//        httpServer.websocket("/ime", "ime-protocol", new AsyncHttpServer.WebSocketRequestCallback() {
//            public void onConnected(WebSocket paramAnonymousWebSocket, AsyncHttpServerRequest paramAnonymousAsyncHttpServerRequest) {
//                if (Main_bak.broadcastIntent == null) {
//                    paramAnonymousWebSocket.close();
//                    return;
//                }
//                paramAnonymousWebSocket.setStringCallback(new WebSocket.StringCallback() {
//                    public void onStringAvailable(String paramAnonymous2String) {
//                        if ("bind".equals(paramAnonymous2String)) {
//                            Main_bak.isImeRunning = true;
//                        }
//                        while (!"unbind".equals(paramAnonymous2String)) {
//                            return;
//                        }
//                        Main_bak.isImeRunning = false;
//                    }
//                });
//                paramAnonymousWebSocket.setClosedCallback(new CompletedCallback() {
//                    public void onCompleted(Exception paramAnonymous2Exception) {
//                        Main_bak.isImeRunning = false;
//                    }
//                });
//            }
//        });
//
//        httpServer.get("/h264", new HttpServerRequestCallback() {
//            public void onRequest(AsyncHttpServerRequest paramAnonymousAsyncHttpServerRequest, AsyncHttpServerResponse paramAnonymousAsyncHttpServerResponse) {
//                try {
//                    Main_bak.turnScreenOn(localObject1, localObject2, localObject41);
//                    paramAnonymousAsyncHttpServerResponse.getHeaders().set("Access-Control-Allow-Origin", "*");
//                    paramAnonymousAsyncHttpServerResponse.getHeaders().set("Connection", "close");
//                    paramAnonymousAsyncHttpServerResponse.setClosedCallback(new CompletedCallback() {
//                        public void onCompleted(Exception paramAnonymous2Exception) {
//                            Log.i("VysorMain", "Connection terminated.");
//                            if (paramAnonymous2Exception != null) {
//                                Log.e("VysorMain", "Error", paramAnonymous2Exception);
//                            }
////                            this.val$device.stop();
//                        }
//                    });
//                    return;
//                } catch (Exception ex) {
//                    ex.printStackTrace();
//                }
//            }
//        });
//
//        Log.i("VysorMain", "Server starting");
//        localAsyncServerSocket = server.listen(null, 53517, new ListenCallback()
//        {
//            StdOutDevice device;
//
//            public void onAccepted(final AsyncSocket paramAnonymousAsyncSocket)
//            {
//                Log.i("VysorMain", "New raw socket accepted.");
//                paramAnonymousAsyncSocket.setClosedCallback(new CompletedCallback()
//                {
//                    public void onCompleted(Exception paramAnonymous2Exception)
//                    {
//                        Log.i("VysorMain", "Connection terminated.");
////                        if (Main.13.this.device != null) {
////                        Main.13.this.device.stop();
//                    }
//                });
//            }
//
//            void onAuthenticated(AsyncSocket paramAnonymousAsyncSocket)
//            {
//                Log.i("VysorMain", "h264 authentication succeeded");
//                paramAnonymousAsyncSocket.setDataCallback(new DataCallback.NullDataCallback());
//                this.device = Main_bak.writer(new BufferedDataSink(paramAnonymousAsyncSocket), localObject51);
//            }
//
//            public void onCompleted(Exception paramAnonymousException) {}
//
//            public void onListening(AsyncServerSocket paramAnonymousAsyncServerSocket) {}
//        });
//
//    }
//
//
////    static class AnonymousClass7 implements HttpServerRequestCallback {
////
////        public void onRequest(AsyncHttpServerRequest paramAnonymousAsyncHttpServerRequest, AsyncHttpServerResponse paramAnonymousAsyncHttpServerResponse)
////        {
////            try
////            {
////                Main.turnScreenOn(this.val$im, localObject2, localObject4);
////                paramAnonymousAsyncHttpServerResponse.getHeaders().set("Access-Control-Allow-Origin", "*");
////                paramAnonymousAsyncHttpServerResponse.getHeaders().set("Connection", "close");
////                paramAnonymousAsyncHttpServerResponse.setClosedCallback(new CompletedCallback()
////                {
////                    public void onCompleted(Exception paramAnonymous2Exception)
////                    {
////                        Log.i("VysorMain", "Connection terminated.");
////                        if (paramAnonymous2Exception != null) {
////                            Log.e("VysorMain", "Error", paramAnonymous2Exception);
////                        }
////                        this.val$device.stop();
////                    }
////                });
////                return;
////            }
////            catch (Exception ex)
////            {
////                ex.printStackTrace();
////            }
////        }
////    }
//
//    public static Bitmap screenshot() throws Exception {
//        String surfaceClassName;
//        Point size = SurfaceControlVirtualDisplayFactory.getCurrentDisplaySize(false);
//        if (Build.VERSION.SDK_INT <= 17) {
//            surfaceClassName = "android.view.Surface";
//        } else {
//            surfaceClassName = "android.view.SurfaceControl";
//        }
//        Bitmap b = (Bitmap) Class.forName(surfaceClassName).getDeclaredMethod("screenshot", new Class[]{Integer.TYPE, Integer.TYPE}).invoke(null, new Object[]{Integer.valueOf(size.x), Integer.valueOf(size.y)});
//        return b;
//    }
//
//    static void sendEvent(JSONObject paramJSONObject)
//    {
//        if ((webSocket instanceof WebSocket))
//        {
//            ((WebSocket)webSocket).send(paramJSONObject.toString());
//            return;
//        }
//        ByteBufferList localByteBufferList = new ByteBufferList();
//        byte[] paramJSONObject1 = (paramJSONObject.toString() + "\n").getBytes();
//        ByteBuffer localByteBuffer = ByteBuffer.allocate(paramJSONObject1.length);
//        localByteBuffer.order(ByteOrder.LITTLE_ENDIAN);
//        localByteBuffer.put(paramJSONObject1);
//        localByteBuffer.flip();
//        localByteBufferList.add(localByteBuffer);
//        ((BufferedDataSink)webSocket).write(localByteBufferList);
//    }
//
//    static void sendDisplayInfo()
//    {
//        Point localPoint = SurfaceControlVirtualDisplayFactory.getCurrentDisplaySize();
//        JSONObject localJSONObject = new JSONObject();
//        try
//        {
//            localJSONObject.put("type", "displaySize");
//            localJSONObject.put("screenWidth", localPoint.x);
//            localJSONObject.put("screenHeight", localPoint.y);
////            localJSONObject.put("nav", hasNavBar());
//            sendEvent(localJSONObject);
////            encodeSizeThrottle.postThrottled(null);
//            return;
//        }
//        catch (JSONException ex)
//        {
//            ex.printStackTrace();
//        }
//    }
//
//
//    private static void turnScreenOn(InputManager paramInputManager, Method paramMethod, IPowerManager paramIPowerManager)
//            throws RemoteException, InvocationTargetException, IllegalAccessException
//    {
//        try
//        {
//            if (!paramIPowerManager.isScreenOn()) {
//                sendKeyEvent(paramInputManager, paramMethod, 257, 26, false);
//            }
//            return;
//        }
//        catch (NoSuchMethodError localNoSuchMethodError)
//        {
//            try
//            {
//                while (paramIPowerManager.isInteractive()) {}
//                sendKeyEvent(paramInputManager, paramMethod, 257, 26, false);
//                return;
//            }
//            catch (NoSuchMethodError ex) {}
//        }
//    }
//
//    private static void sendKeyEvent(InputManager paramInputManager, Method paramMethod, int paramInt1, int paramInt2, boolean paramBoolean)
//            throws InvocationTargetException, IllegalAccessException
//    {
//        long l = SystemClock.uptimeMillis();
//        if (paramBoolean) {}
//        for (int i = 1;; i = 0)
//        {
//            injectKeyEvent(paramInputManager, paramMethod, new KeyEvent(l, l, 0, paramInt2, 0, i, -1, 0, 0, paramInt1));
//            injectKeyEvent(paramInputManager, paramMethod, new KeyEvent(l, l, 1, paramInt2, 0, i, -1, 0, 0, paramInt1));
//            return;
//        }
//    }
//    private static void injectKeyEvent(InputManager paramInputManager, Method paramMethod, KeyEvent paramKeyEvent)
//            throws InvocationTargetException, IllegalAccessException
//    {
//        paramMethod.invoke(paramInputManager, new Object[] { paramKeyEvent, Integer.valueOf(0) });
//    }
//
//    static StdOutDevice writer(BufferedDataSink paramBufferedDataSink, IWindowManager paramIWindowManager)
//    {
//        if (current != null)
//        {
//            current.stop();
//            current = null;
//        }
//        Point localPoint = getEncodeSize();
//        Object localObject = new SurfaceControlVirtualDisplayFactory();
//        StdOutDevice paramBufferedDataSink = new StdOutDevice(localPoint.x, localPoint.y, paramBufferedDataSink);
//        if (resolution != 0.0D) {
//            paramBufferedDataSink.setUseEncodingConstraints(false);
//        }
//        if (Build.VERSION.SDK_INT < 19) {
//            paramBufferedDataSink.useSurface(false);
//        }
//        current = paramBufferedDataSink;
//        Log.i("VysorMain", "registering virtual display");
//        if (paramBufferedDataSink.supportsSurface()) {
//            paramBufferedDataSink.registerVirtualDisplay(null, (VirtualDisplayFactory)localObject, 0);
//        }
//        for (;;)
//        {
//            Log.i("VysorMain", "virtual display registered");
//            return paramBufferedDataSink;
//            Log.i("VysorMain", "Using legacy path");
//            paramBufferedDataSink.createDisplaySurface();
//            localObject = new EncoderFeeder(paramBufferedDataSink.getMediaCodec(), paramBufferedDataSink.getEncodingDimensions().x, paramBufferedDataSink.getEncodingDimensions().y, paramBufferedDataSink.getColorFormat());
//            try
//            {
//                paramIWindowManager.watchRotation(new IRotationWatcher.Stub()
//                {
//                    public void onRotationChanged(int paramAnonymousInt)
//                            throws RemoteException
//                    {
//                        this.val$feeder.setRotation(paramAnonymousInt);
//                    }
//                });
//                ((EncoderFeeder)localObject).feed();
//            }
//            catch (RemoteException paramIWindowManager)
//            {
//                for (;;) {}
//            }
//        }
//    }
//
//    static Point getEncodeSize()
//    {
//        Point localPoint = SurfaceControlVirtualDisplayFactory.getCurrentDisplaySize();
//        if (resolution != 0.0D)
//        {
//            localPoint.x = ((int)(localPoint.x * resolution));
//            localPoint.y = ((int)(localPoint.y * resolution));
//        }
//        for (;;)
//        {
//            return localPoint;
//            if ((localPoint.x >= 1280) || (localPoint.y >= 1280)) {
//                localPoint.x /= 2;
//            }
//            for (localPoint.y /= 2; (localPoint.x > 1280) || (localPoint.y > 1280); localPoint.y /= 2) {
//                localPoint.x /= 2;
//            }
//        }
//    }
//}
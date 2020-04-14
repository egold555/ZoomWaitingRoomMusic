import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinUser;

public interface CustomUser32 extends User32 {

	@SuppressWarnings("deprecation")
	CustomUser32 INSTANCE = (CustomUser32) Native.loadLibrary("user32", CustomUser32.class);
	boolean EnumWindows(WinUser.WNDENUMPROC lpEnumFunc, Pointer arg);
	int GetWindowTextA(HWND hWnd, byte[] lpString, int nMaxCount);
}

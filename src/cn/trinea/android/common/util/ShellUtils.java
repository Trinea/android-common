package cn.trinea.android.lib.util;

import android.os.Build;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * ShellUtils
 * <ul>
 * <strong>Check root</strong>
 * <li>{@link ShellUtils#checkRootPermission()}</li>
 * </ul>
 * <ul>
 * <strong>Execte command</strong>
 * <li>{@link ShellUtils#execCommand(String)}</li>
 * <li>{@link ShellUtils#execCommand(String, long)}</li>
 * <li>{@link ShellUtils#execCommand(String, boolean)}</li>
 * <li>{@link ShellUtils#execCommand(String, boolean, long)}</li>
 * <li>{@link ShellUtils#execCommand(String, boolean, boolean)}</li>
 * <li>{@link ShellUtils#execCommand(String, boolean, long, boolean)}</li>
 * <li>{@link ShellUtils#execCommand(List, boolean)}</li>
 * <li>{@link ShellUtils#execCommand(List, boolean, long)}</li>
 * <li>{@link ShellUtils#execCommand(List, boolean, long, boolean)}</li>
 * <li>{@link ShellUtils#execCommand(String[], boolean)}</li>
 * <li>{@link ShellUtils#execCommand(String[], boolean, long)}</li>
 * <li>{@link ShellUtils#execCommand(String[], boolean, long, boolean)}</li>
 * </ul>
 *
 * @author <a href="https://www.trinea.cn" target="_blank">Trinea</a> 2013-5-16
 */
public class ShellUtils {

    public static final String COMMAND_SU = "su";
    public static final String COMMAND_SH = "sh";
    public static final String COMMAND_EXIT = "exit\n";
    public static final String COMMAND_LINE_END = "\n";

    private ShellUtils() {
        throw new AssertionError();
    }

    /**
     * check whether has root permission
     *
     * @return
     */
    public static boolean checkRootPermission() {
        return execCommand("echo root", true, 5000, false).result == 0;
    }

    public static CommandResult execCommand(String command) {
        return execCommand(new String[]{command}, false, -1, true);
    }

    public static CommandResult execCommand(String command, long timeoutInMills) {
        return execCommand(new String[]{command}, false, timeoutInMills, true);
    }

    public static CommandResult execCommand(String command, boolean isRoot) {
        return execCommand(new String[]{command}, isRoot, -1, true);
    }

    /**
     * execute shell command, default return result msg
     *
     * @param command        command
     * @param isRoot         whether need to run with root
     * @param timeoutInMills timeout in millSeconds, -1 means no timeout, if bigger than 0, you must make sure Android Version is not smaller than Android O
     * @return
     * @see ShellUtils#execCommand(String[], boolean, long, boolean)
     */
    public static CommandResult execCommand(String command, boolean isRoot, long timeoutInMills) {
        return execCommand(new String[]{command}, isRoot, timeoutInMills, true);
    }

    public static CommandResult execCommand(List<String> commands, boolean isRoot) {
        return execCommand(commands == null ? null : commands.toArray(new String[]{}), isRoot, -1, true);
    }

    /**
     * execute shell commands, default return result msg
     *
     * @param commands       command list
     * @param isRoot         whether need to run with root
     * @param timeoutInMills timeout in millSeconds, -1 means no timeout, if bigger than 0, you must make sure Android Version is not smaller than Android O
     * @return
     * @see ShellUtils#execCommand(String[], boolean, long, boolean)
     */
    public static CommandResult execCommand(List<String> commands, boolean isRoot, long timeoutInMills) {
        return execCommand(commands == null ? null : commands.toArray(new String[]{}), isRoot, timeoutInMills, true);
    }

    public static CommandResult execCommand(String[] commands, boolean isRoot) {
        return execCommand(commands, isRoot, -1, true);
    }

    /**
     * execute shell commands, default return result msg
     *
     * @param commands       command array
     * @param isRoot         whether need to run with root
     * @param timeoutInMills timeout in millSeconds, -1 means no timeout, if bigger than 0, you must make sure Android Version is not smaller than Android O
     * @return
     * @see ShellUtils#execCommand(String[], boolean, long, boolean)
     */
    public static CommandResult execCommand(String[] commands, boolean isRoot, long timeoutInMills) {
        return execCommand(commands, isRoot, timeoutInMills, true);
    }

    public static CommandResult execCommand(String command, boolean isRoot, boolean isNeedResultMsg) {
        return execCommand(new String[]{command}, isRoot, -1, isNeedResultMsg);
    }

    /**
     * execute shell command
     *
     * @param command         command
     * @param isRoot          whether need to run with root
     * @param timeoutInMills  timeout in millSeconds, -1 means no timeout, if bigger than 0, you must make sure Android Version is not smaller than Android O
     * @param isNeedResultMsg whether need result msg
     * @return
     * @see ShellUtils#execCommand(String[], boolean, long, boolean)
     */
    public static CommandResult execCommand(String command, boolean isRoot, long timeoutInMills, boolean isNeedResultMsg) {
        return execCommand(new String[]{command}, isRoot, timeoutInMills, isNeedResultMsg);
    }

    /**
     * execute shell commands
     *
     * @param commands        command list
     * @param isRoot          whether need to run with root
     * @param timeoutInMills  timeout in millSeconds, -1 means no timeout, if bigger than 0, you must make sure Android Version is not smaller than Android O
     * @param isNeedResultMsg whether need result msg
     * @return
     * @see ShellUtils#execCommand(String[], boolean, long, boolean)
     */
    public static CommandResult execCommand(List<String> commands, boolean isRoot, long timeoutInMills, boolean isNeedResultMsg) {
        return execCommand(commands == null ? null : commands.toArray(new String[]{}), isRoot, timeoutInMills, isNeedResultMsg);
    }

    /**
     * execute shell commands
     *
     * @param commands        command array
     * @param isRoot          whether need to run with root
     * @param timeoutInMills  timeout in millSeconds, -1 means no timeout, if bigger than 0, you must make sure Android Version is not smaller than Android O
     * @param isNeedResultMsg whether need result msg
     * @return <ul>
     * <li>if isNeedResultMsg is false, {@link CommandResult#successMsg} is null and
     * {@link CommandResult#errorMsg} is null.</li>
     * <li>if {@link CommandResult#result} is -1, there maybe some excepiton.</li>
     * </ul>
     */
    public static CommandResult execCommand(String[] commands, boolean isRoot, long timeoutInMills, boolean isNeedResultMsg) {
        int result = -1;
        if (commands == null || commands.length == 0) {
            return new CommandResult(result, null, null);
        }

        Process process = null;
        BufferedReader successResult = null;
        BufferedReader errorResult = null;
        StringBuilder successMsg = null;
        StringBuilder errorMsg = null;

        DataOutputStream os = null;
        boolean isTimeout = false;
        try {
            process = Runtime.getRuntime().exec(isRoot ? COMMAND_SU : COMMAND_SH);
            os = new DataOutputStream(process.getOutputStream());
            for (String command : commands) {
                if (command == null) {
                    continue;
                }

                // donnot use os.writeBytes(commmand), avoid chinese charset error
                os.write(command.getBytes());
                os.writeBytes(COMMAND_LINE_END);
                os.flush();
            }
            os.writeBytes(COMMAND_EXIT);
            os.flush();

            if (timeoutInMills <= 0) {
                result = process.waitFor();
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    isTimeout = process.waitFor(timeoutInMills, TimeUnit.MILLISECONDS);
                    result = isTimeout ? 0 : -1;
                }
            }
            // get command result
            if (isNeedResultMsg) {
                successMsg = new StringBuilder();
                errorMsg = new StringBuilder();
                successResult = new BufferedReader(new InputStreamReader(process.getInputStream()));
                errorResult = new BufferedReader(new InputStreamReader(process.getErrorStream()));
                String s;
                while ((s = successResult.readLine()) != null) {
                    successMsg.append(s).append(COMMAND_LINE_END);
                }
                while ((s = errorResult.readLine()) != null) {
                    errorMsg.append(s).append(COMMAND_LINE_END);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (os != null) {
                    os.close();
                }
                if (successResult != null) {
                    successResult.close();
                }
                if (errorResult != null) {
                    errorResult.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (process != null) {
                process.destroy();
            }
        }
        if (isTimeout && errorMsg.length() == 0) {
            errorMsg.append("Timeout, possibly due to lack of permissions.");
        }
        return new CommandResult(result, successMsg == null ? null : successMsg.toString(), errorMsg == null ? null
                : errorMsg.toString());
    }

    /**
     * result of command
     * <ul>
     * <li>{@link CommandResult#result} means result of command, 0 means normal, else means error, same to excute in
     * linux shell</li>
     * <li>{@link CommandResult#successMsg} means success message of command result</li>
     * <li>{@link CommandResult#errorMsg} means error message of command result</li>
     * </ul>
     *
     * @author <a href="https://www.trinea.cn" target="_blank">Trinea</a> 2013-5-16
     */
    public static class CommandResult {

        /**
         * result of command
         **/
        public int result;
        /**
         * success message of command result
         **/
        public String successMsg;
        /**
         * error message of command result
         **/
        public String errorMsg;

        public CommandResult(int result) {
            this.result = result;
        }

        public CommandResult(int result, String successMsg, String errorMsg) {
            this.result = result;
            this.successMsg = successMsg;
            this.errorMsg = errorMsg;
        }

        @Override
        public String toString() {
            return "result is: " + result +
                    "\nsuccessMsg is: '" + successMsg + "'" +
                    "\nerrorMsg is: '" + errorMsg + "'";
        }
    }
}

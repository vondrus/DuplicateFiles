import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class UserControl {
    private Scanner scanner = new Scanner(System.in);
    private Pattern pattern = Pattern.compile("([CcPpRrSs])([0-9]{0,4})");

    Couple<UserControlCommand, Integer> getCommand() {
        Couple<UserControlCommand, Integer> returnValue = null;
        String inputString = scanner.nextLine();

        if (inputString.length() > 0) {
            Matcher matcher = pattern.matcher(inputString);

            if (matcher.matches()) {
                UserControlCommand command;
                int threadNumber;

                switch (matcher.group(1).toUpperCase()) {
                    case "C":
                        command = UserControlCommand.CONTINUE;
                        break;

                    case "P":
                        command = UserControlCommand.SUSPEND;
                        break;

                    case "R":
                        command = UserControlCommand.RESTART;
                        break;

                    case "S":
                        command = UserControlCommand.STOP;
                        break;

                    default:
                        command = UserControlCommand.NONE;
                        break;
                }

                if (matcher.group(2).length() > 0) {
                    threadNumber = Integer.parseUnsignedInt(matcher.group(2));
                }
                else {
                    threadNumber = -1;
                }

                returnValue = new Couple<>(command, threadNumber);
            }
            else {
                returnValue = new Couple<>(UserControlCommand.INCORRECT, -1);
            }

        }

        return returnValue;

    }

}

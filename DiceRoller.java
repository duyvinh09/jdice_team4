import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * DiceRoller: Lớp mô phỏng việc lăn nhiều xúc xắc với số mặt và bonus tuỳ chỉnh.
 *
 * Refactor:
 * - Đổi tên class từ DieRoll → DiceRoller để mô tả rõ chức năng.
 * - Đổi tên biến từ ndice → numDice, nsides → numSides giúp dễ hiểu hơn.
 * - Đơn giản hóa logic hàm toString() bằng StringBuilder.
 * - Thêm kiểm tra đầu vào trong constructor để tránh lỗi khi nhập số xúc xắc/mặt không hợp lệ.
 *
 * Chức năng thêm:
 * - Logging quá trình lăn xúc xắc bằng java.util.logging.
 * - Phương thức helper averageRollEstimate() để ước lượng trung bình kết quả lăn.
 * - Logging tổng giá trị sau khi lăn.
 */
public class DiceRoller {

    private final int numDice;     // Refactor: Đổi từ ndice → numDice cho rõ nghĩa
    private final int numSides;    // Refactor: Đổi từ nsides → numSides cho rõ nghĩa
    private final int bonus;

    private static final Random rnd = new Random();  // Refactor: đưa rnd thành final
    private static final Logger logger = Logger.getLogger(DiceRoller.class.getName());  // Chức năng thêm: Logging

    /**
     * Constructor có kiểm tra dữ liệu đầu vào.
     * @param numDice số lượng xúc xắc (phải > 0)
     * @param numSides số mặt trên mỗi xúc xắc (phải > 0)
     * @param bonus điểm cộng thêm
     *
     * Refactor + Chức năng thêm:
     * - Thêm validation dữ liệu đầu vào: số xúc xắc và số mặt phải dương
     */
    public DiceRoller(int numDice, int numSides, int bonus) {
        if (numDice < 1 || numSides < 1) {
            throw new IllegalArgumentException("Số xúc xắc và số mặt phải lớn hơn 0.");
        }
        this.numDice = numDice;
        this.numSides = numSides;
        this.bonus = bonus;
    }

    /**
     * Lăn xúc xắc và tính tổng, có logging từng lần lăn.
     * @return đối tượng RollResult chứa kết quả từng lần và tổng.
     *
     * Refactor: dùng biến tên rõ nghĩa hơn, gọn hơn.
     * Chức năng thêm: ghi log từng roll và tổng kết quả cuối cùng.
     */
    public RollResult makeRoll() {
        RollResult result = new RollResult(bonus);
        for (int i = 0; i < numDice; i++) {
            int roll = rnd.nextInt(numSides) + 1;
            result.addResult(roll);
            logger.log(Level.INFO, "Đã lăn: " + roll);  // Chức năng thêm: log kết quả từng lần lăn
        }
        logger.log(Level.INFO, "Tổng cộng với bonus: " + result.getTotal());  // Chức năng thêm
        return result;
    }

    /**
     * Tính giá trị trung bình ước lượng khi lăn xúc xắc.
     * @return giá trị trung bình lý thuyết.
     *
     * Chức năng thêm: tiện ích ước lượng kết quả để tham khảo nhanh.
     */
    public double averageRollEstimate() {
        return numDice * (numSides + 1) / 2.0 + bonus;
    }

    /**
     * Tạo chuỗi mô tả cú pháp lăn xúc xắc (VD: "3d6+2").
     * @return chuỗi mô tả lệnh lăn xúc xắc.
     *
     * Refactor: dùng StringBuilder để hiệu suất tốt hơn và dễ đọc.
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(numDice).append("d").append(numSides);
        if (bonus > 0) {
            sb.append("+").append(bonus);
        } else if (bonus < 0) {
            sb.append(bonus); // đã có dấu "-"
        }
        return sb.toString();
    }
}

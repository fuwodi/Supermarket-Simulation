package supermarket.customer;

import java.util.Random;

public class DiscountCard {
    private String cardId;
    private int points;
    private Random random;

    public DiscountCard(String cardId) {
        this.cardId = cardId;
        this.points = 50;
        this.random = new Random();
    }

    public void addPoints(double purchaseAmount) {
        this.points += (int)(purchaseAmount * 0.05); // 5% от суммы в баллы
    }

    public double usePoints(double purchaseAmount) {
        if (points > 0 && random.nextDouble() < 0.3) { // 30% шанс потратить
            int maxPoints = Math.min(points, (int)(purchaseAmount * 0.7));
            int pointsToUse = random.nextInt(maxPoints + 1);

            if (pointsToUse > 0) {
                points -= pointsToUse;
                return pointsToUse; // 1 балл = 1 рубль
            }
        }
        return 0;
    }

    public String getCardId() { return cardId; }
    public int getPoints() { return points; }

    @Override
    public String toString() {
        return cardId + " [" + points + " баллов]";
    }
}
package me.ivanmorozov.telegrambot;

import me.ivanmorozov.telegrambot.service.CalculatorService;
import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
public class CalculatorServiceTest {
  private final  CalculatorService calculatorService = new CalculatorService();

  @Test
  public void add_shouldReturnSumOfTwoInteger(){
      int a = 1;
      int b = 2;

     int result = calculatorService.add(a,b);
     assertEquals(3,result,"Метод должен сложить 2 числа");
  }

  @Test
  public void divided_shouldReturnIllegalArgument(){
      int a = 4;
      int b = 0;

      IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
              ()-> calculatorService.divide(a,b),
              "Ожидалось IllegalArgumentException при делении на ноль");

      assertEquals("Divider cannot be zero", exception.getMessage());
  }
}

package riggy_software.avenade.shopping;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.junit.Test;

/**
 * 
 * @author Naseem Regragui
 * 
 * 
 * Exercise:
 *
 * Using Java, write a simple program that calculates the price of a basket of shopping.
 * The solution should be accomplished in roughly two hours.
 *
 *
 * Items are presented one at a time, in a list, identified by name - for example "Apple" or "Banana".
 * Multiple items are present multiple times in the list,
 * so for example ["Apple", "Apple", "Banana"] is a basket with two apples and one banana.
 *
 * Items are priced as follows:
 * - Apples are 35p each
 * - Bananas are 20p each
 * - Melons are 50p each, but are available as ‘buy one get one free’
 * - Limes are 15p each, but are available in a ‘three for the price of two’ offer
 *
 * Given a list of shopping, calculate the total cost of those items.
 *
 */
public class ShoppingCartTest {

	public interface Visitor {

		public void visit(ShoppingCart cart);
	}
	
	public interface Visitable {

		public void accept(Visitor v);
	}
	
	public interface Discount {
		
		public Double getTotalDiscount();
	}
	
	public static class ShoppingPrices {
		public static Map<String, Double> prices;
		
		static {
			prices  = new HashMap<String, Double>();
			prices.put("Apple", 0.35d);
			prices.put("Banana", 0.20d);
			prices.put("Melon", 0.50d);
			prices.put("Lime", 0.15d);
		}
	}
	
	public class BOGOF implements Discount, Visitor {

		private String bogof;
		private double totalDiscount = 0;
		
		public BOGOF(String item) {
			bogof = item;
		}
		
		@Override
		public void visit(ShoppingCart cart) {
			
			long count = cart.getItems().stream()
							.filter(item -> bogof.equals(item))
							.count();
							if (count%2 != 0) {
								count -=1;
							} 
							long discountable = count/2;
							totalDiscount += discountable * ShoppingPrices.prices.get(bogof);
		}
							
		@Override
		public Double getTotalDiscount() {
			return totalDiscount;
		}
		
	}
	
	public class ThreeForTwo implements Discount, Visitor {

		private double totalDiscount = 0;
		private String discountItem;
		
		public ThreeForTwo(String discountItem) {
			this.discountItem = discountItem;
		}
		
		@Override
		public void visit(ShoppingCart cart) {
			long count = cart.getItems().stream()
					.filter(item -> discountItem.equals(item))
					.count();
					long discountable = count/3;
					totalDiscount += discountable * ShoppingPrices.prices.get(discountItem);
		}
		
		@Override
		public Double getTotalDiscount() {
			return totalDiscount;
		}
	}
	
	public class ItemsTotaller implements Visitor {

		private double itemsTotal;

		public void visit(ShoppingCart cart) {
			
			itemsTotal = cart.getItems()
					.stream()
					.mapToDouble(s -> ShoppingPrices.prices.get(s))
					.sum();	
		}

		public double getItemsTotal() {
			return itemsTotal;
		}
		
	}
	
	class ShoppingCart implements Visitable {
		
		List<String> items;
		
		public ShoppingCart(){
			items = new LinkedList<String>();
		}
		
		public void addItem(String item) {
			items.add(item);
		}
		
		public void removeItem(String item) {
			items.remove(item);
		}
		
		public List<String> getItems(){
			return items;
		}

		public void accept(Visitor v) {
			v.visit(this);
			
		}
	}
	
	public class Cashier implements Visitor {
		
		private List<Visitor> discounts;
		private ItemsTotaller itemsTotaller;
		private double totalCharge = 0;
		
		public Cashier() {
			itemsTotaller = new ItemsTotaller();
			discounts = Arrays.asList(new BOGOF("Melon"), new ThreeForTwo("Lime"));
		}


		@Override
		public void visit(ShoppingCart cart) {
			cart.accept(itemsTotaller);
			discounts.forEach(discount -> cart.accept(discount));
			
			totalCharge = itemsTotaller.getItemsTotal() - discounts
					.stream()
					.map(v -> (Discount)v)
					.mapToDouble(Discount::getTotalDiscount).sum();
			
		}


		public double getTotalCharge() {
			return totalCharge;
		}
		
	}
	
	@Test
	public void testItemPrices() {
		
		Map<String, Double> prices = ShoppingPrices.prices;
		
		assertEquals("Apple priced incorrectly", 0.35d, prices.get("Apple"), 0.0);
		assertEquals("Bananas priced incorrectly", 0.20d, prices.get("Banana"), 0.0);
		assertEquals("Melons priced incorrectly", 0.50d, prices.get("Melon"), 0.0);
		assertEquals("Limes priced incorrectly", 0.15d, prices.get("Lime"), 0.0);
	}
	
	@Test
	public void testItemsTotaller() {
		ShoppingCart cart = new ShoppingCart();
		cart.addItem("Apple");
		cart.addItem("Banana");
		cart.addItem("Melon");
		cart.addItem("Lime");
		
		ItemsTotaller itemsTotaller = new ItemsTotaller(); 
		itemsTotaller.visit(cart);
		
		assertEquals("Total not correct", 1.20, itemsTotaller.getItemsTotal(), 0.00);
	}
	
	@Test
	public void testBOGOF() {
		
		//Normally I would have each case below in a separate test method

		ShoppingCart cart = new ShoppingCart();
		BOGOF bogof = new BOGOF("Apple");
		cart.accept(bogof);
		
		assertEquals("bogof discount miscalculation", 0.0, bogof.getTotalDiscount(), 0.0);
		
		bogof = new BOGOF("Melon");
		cart = new ShoppingCart();
		cart.addItem("Melon");
		cart.accept(bogof);
		
		assertEquals("bogof discount miscalculation", 0.0, bogof.getTotalDiscount(), 0.0);

		bogof = new BOGOF("Melon");
		cart = new ShoppingCart();
		cart.addItem("Melon");
		cart.addItem("Melon");
		cart.accept(bogof);
		
		assertEquals("bogof discount miscalculation", 0.50, bogof.getTotalDiscount(), 0.0);
		
		bogof = new BOGOF("Melon");
		cart = new ShoppingCart();
		cart.addItem("Melon");
		cart.addItem("Melon");
		cart.addItem("Melon");
		cart.accept(bogof);

		assertEquals("bogof discount miscalculation", 0.50, bogof.getTotalDiscount(), 0.0);
		
		bogof = new BOGOF("Melon");
		cart = new ShoppingCart();
		cart.addItem("Melon");
		cart.addItem("Melon");
		cart.addItem("Melon");
		cart.addItem("Melon");
		cart.accept(bogof);
		
		assertEquals("bogof discount miscalculation", 1.00, bogof.getTotalDiscount(), 0.0);
		
		bogof = new BOGOF("Melon");
		cart = new ShoppingCart();
		cart.addItem("Melon");
		cart.addItem("Melon");
		cart.addItem("Melon");
		cart.addItem("Melon");
		cart.addItem("Melon");
		cart.accept(bogof);
		
		assertEquals("bogof discount miscalculation", 1.00, bogof.getTotalDiscount(), 0.0);
		
	}
	
	@Test
	public void testThreeForTwo() {
		//Normally I would have each case below in a separate test method
		ShoppingCart cart = new ShoppingCart();
		ThreeForTwo t4t = new ThreeForTwo("Lime");
		cart.addItem("Lime");
		cart.accept(t4t);
		assertEquals("ThreeForTwo discount miscalculation", 0.0, t4t.getTotalDiscount(), 0.0);
		
		cart = new ShoppingCart();
		t4t = new ThreeForTwo("Lime");
		cart.addItem("Lime");
		cart.addItem("Lime");
		cart.accept(t4t);
		assertEquals("ThreeForTwo discount miscalculation", 0.0, t4t.getTotalDiscount(), 0.0);
		
		cart = new ShoppingCart();
		t4t = new ThreeForTwo("Lime");
		cart.addItem("Lime");
		cart.addItem("Lime");
		cart.addItem("Lime");
		cart.accept(t4t);
		assertEquals("ThreeForTwo discount miscalculation", 0.15, t4t.getTotalDiscount(), 0.0);
		
		cart = new ShoppingCart();
		t4t = new ThreeForTwo("Lime");
		cart.addItem("Lime");
		cart.addItem("Lime");
		cart.addItem("Lime");
		cart.addItem("Lime");
		cart.accept(t4t);
		assertEquals("ThreeForTwo discount miscalculation", 0.15, t4t.getTotalDiscount(), 0.0);
		
		cart = new ShoppingCart();
		t4t = new ThreeForTwo("Lime");
		cart.addItem("Lime");
		cart.addItem("Lime");
		cart.addItem("Lime");
		cart.addItem("Lime");
		cart.addItem("Lime");
		cart.accept(t4t);
		assertEquals("ThreeForTwo discount miscalculation", 0.15, t4t.getTotalDiscount(), 0.0);
		
		cart = new ShoppingCart();
		t4t = new ThreeForTwo("Lime");
		cart.addItem("Lime");
		cart.addItem("Lime");
		cart.addItem("Lime");
		cart.addItem("Lime");
		cart.addItem("Lime");
		cart.addItem("Lime");
		cart.accept(t4t);
		assertEquals("ThreeForTwo discount miscalculation", 0.30, t4t.getTotalDiscount(), 0.0);
	}
	
	@Test
	public void testCashier() {
		ShoppingCart cart = new ShoppingCart();
		cart.addItem("Apple");
		cart.addItem("Banana");
		cart.addItem("Melon");
		cart.addItem("Melon");
		cart.addItem("Lime");
		cart.addItem("Lime");
		cart.addItem("Lime");
		Cashier cashier = new Cashier();
		cart.accept(cashier);
		
		
		assertEquals("Cashier miscalculation", 1.35, cashier.getTotalCharge(), 0.0);
		
	}
}

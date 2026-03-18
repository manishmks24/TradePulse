import requests
import concurrent.futures
import time

API_URL = "http://localhost:8080/api/orders"

def place_order(user_id, stock_id, order_type, price, quantity):
    payload = {
        "userId": user_id,
        "stockId": stock_id,
        "type": order_type,
        "price": price,
        "quantity": quantity
    }
    response = requests.post(API_URL, json=payload)
    return response.status_code, response.text

def run_concurrent_test():
    orders_to_place = []
    
    # User 1 (Alice) buys AAPL, User 2 (Bob) sells AAPL
    # Both try to submit 50 orders of 1 share
    for _ in range(50):
        orders_to_place.append((1, 1, "BID", 150.00, 1))
        orders_to_place.append((2, 1, "ASK", 150.00, 1))

    print(f"Submitting {len(orders_to_place)} orders concurrently...")
    
    success_count = 0
    fail_count = 0
    
    start_time = time.time()
    
    with concurrent.futures.ThreadPoolExecutor(max_workers=20) as executor:
        futures = [executor.submit(place_order, *order) for order in orders_to_place]
        for future in concurrent.futures.as_completed(futures):
            status, text = future.result()
            if status == 200:
                success_count += 1
            else:
                fail_count += 1
                
    end_time = time.time()
    
    print(f"Test completed in {end_time - start_time:.2f} seconds.")
    print(f"Successful orders: {success_count}")
    print(f"Failed orders: {fail_count}")
    
    # Check final balances using APIs
    print("\nVerifying Alice's portfolio:")
    alice_cash = requests.get("http://localhost:8080/api/portfolios/1/cash").text
    alice_stocks = requests.get("http://localhost:8080/api/portfolios/1").json()
    print(f"Alice cash: ${alice_cash}")
    print(f"Alice stocks: {alice_stocks}")

if __name__ == "__main__":
    run_concurrent_test()

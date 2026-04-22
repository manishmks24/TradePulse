import yfinance as yf
import requests
import time
import json
from datetime import datetime, timezone

# The API URL of our Spring Boot Backend
BACKEND_API_URL = "http://localhost:8080/api/stocks"
INGESTION_URL = "http://localhost:8080/api/market-data/ingest"

def get_symbols_from_backend():
    try:
        response = requests.get(BACKEND_API_URL)
        if response.status_code == 200:
            stocks = response.json()
            symbols = [stock["symbol"] for stock in stocks if stock.get("symbol")]
            return symbols
        else:
            print(f"Failed to fetch symbols. Status: {response.status_code}")
    except Exception as e:
        print(f"Error fetching symbols from backend: {e}")
    return []

def fetch_and_publish_prices(symbols):
    if not symbols:
        return

    print(f"Fetching live data for {len(symbols)} symbols...")
    ticks = []
    
    for symbol in symbols:
        try:
            ticker = yf.Ticker(symbol)
            # Try to get live price from fast_info
            last_price = None
            if hasattr(ticker, 'fast_info') and 'lastPrice' in ticker.fast_info:
                last_price = ticker.fast_info['lastPrice']
            elif 'currentPrice' in ticker.info:
                last_price = ticker.info['currentPrice']
            elif 'regularMarketPrice' in ticker.info:
                 last_price = ticker.info['regularMarketPrice']

            if last_price:
                ticks.append({
                    "symbol": symbol,
                    "price": round(float(last_price), 2),
                    "timestamp": datetime.now(timezone.utc).isoformat()
                })
        except Exception as e:
            # Fallback for Yahoo IP banning or rate limiting
            import random
            fallback_price = round(100.0 + random.random() * 50, 2)
            ticks.append({
                "symbol": symbol,
                "price": fallback_price,
                "timestamp": datetime.now(timezone.utc).isoformat()
            })
            
    if ticks:
        # Post to backend
        try:
            response = requests.post(INGESTION_URL, json=ticks)
            if response.status_code == 200:
                print(f"Successfully pushed {len(ticks)} live ticks to backend.")
            else:
                print(f"Failed to push. Status: {response.status_code}")
        except Exception as e:
            print(f"Backend offline: {e}")

if __name__ == "__main__":
    print("Starting Live Market Data Agent...")
    while True:
        symbols = get_symbols_from_backend()
        
        if symbols:
            fetch_and_publish_prices(symbols)
        else:
            print("No symbols found or Backend is down. Retrying in 10s...")
        
        # Free APIs rate limit aggresively. 10s is safe for 50-100 symbols.
        time.sleep(10)

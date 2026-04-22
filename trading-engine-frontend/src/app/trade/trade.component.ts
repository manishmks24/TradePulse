import { Component, OnInit, OnDestroy, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { Subscription } from 'rxjs';
import { TradingService, MarketTick, Portfolio, TradeOrder, OrderBookDto } from '../services/trading.service';

@Component({
  selector: 'app-trade',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './trade.component.html',
  styleUrls: ['./trade.component.css']
})
export class TradeComponent implements OnInit, OnDestroy {
  marketData: { [symbol: string]: MarketTick } = {};
  portfolio: Portfolio[] = [];
  cashBalance: number = 0;
  recentTrades: any[] = [];
  orderBooks: { [symbol: string]: OrderBookDto } = {};
  
  // For the sake of demo, hardcode user ID to Alice (1)
  userId = 1;

  // Order Form
  orderSymbol: number = 1; // Default to AAPL
  orderType: string = 'BID';
  orderPrice: number = 0;
  orderQuantity: number = 0;

  currentCategory: string = 'stocks';

  allStocks: any[] = [];

  availableStocks: any[] = [];

  private tickSub!: Subscription;
  private tradeSub!: Subscription;
  private orderBookSub!: Subscription;

  constructor(
    private tradingService: TradingService, 
    private cdr: ChangeDetectorRef,
    private route: ActivatedRoute
  ) {}

  ngOnInit() {
    this.route.queryParams.subscribe(params => {
      this.currentCategory = params['category'] || 'stocks';
      this.tradingService.getStocksByCategory(this.currentCategory).subscribe(stocks => {
        this.availableStocks = stocks;
        if (this.availableStocks.length > 0) {
          this.orderSymbol = this.availableStocks[0].id;
        }
        this.cdr.detectChanges();
      });
    });

    this.fetchPortfolio();

    this.tickSub = this.tradingService.getMarketTicks().subscribe(tick => {
      const existing = this.marketData[tick.symbol];
      let trend = 'unchanged';
      if (existing) {
        trend = tick.price > existing.price ? 'up' : 'down';
      }
      this.marketData[tick.symbol] = { ...tick, trend } as any;
      this.cdr.detectChanges();
    });

    this.tradeSub = this.tradingService.getTradeUpdates().subscribe(trade => {
      this.recentTrades.unshift(trade);
      if (this.recentTrades.length > 10) this.recentTrades.pop();
      // Update portfolio on trade execution
      this.fetchPortfolio();
    });

    this.orderBookSub = this.tradingService.getOrderBookUpdates().subscribe(book => {
      this.orderBooks[book.symbol] = book;
      this.cdr.detectChanges();
    });
  }

  ngOnDestroy() {
    this.tickSub?.unsubscribe();
    this.tradeSub?.unsubscribe();
    this.orderBookSub?.unsubscribe();
  }

  getSelectedSymbol(): string {
    return this.availableStocks.find(s => s.id == this.orderSymbol)?.symbol || 'AAPL';
  }

  getMarketDataKeys() {
    return Object.keys(this.marketData);
  }

  fetchPortfolio() {
    this.tradingService.getPortfolio(this.userId).subscribe(p => this.portfolio = p);
    this.tradingService.getCashBalance(this.userId).subscribe(c => this.cashBalance = c);
  }

  placeOrder() {
    if (this.orderPrice <= 0 || this.orderQuantity <= 0) {
      alert("Invalid price or quantity");
      return;
    }

    const payload = {
      userId: this.userId,
      stockId: this.orderSymbol,
      type: this.orderType,
      price: this.orderPrice,
      quantity: this.orderQuantity
    };

    console.log("Placing order", payload);
    this.tradingService.placeOrder(payload).subscribe({
      next: (res: any) => {
        alert(`Order placed successfully! ID: ${res.id}`);
        this.fetchPortfolio(); // Refresh balances/stocks locked
      },
      error: (err: any) => {
        alert(`Order failed: ${err.error?.message || err.message}`);
      }
    });
  }
}

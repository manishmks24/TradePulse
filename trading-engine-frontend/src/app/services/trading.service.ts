import { Injectable, Inject, PLATFORM_ID } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, Subject } from 'rxjs';
import { Client, Message } from '@stomp/stompjs';
import { isPlatformBrowser } from '@angular/common';

export interface Stock {
  id: number;
  symbol: string;
  companyName: string;
}

export interface Portfolio {
  stockId: number;
  symbol: string;
  companyName: string;
  quantity: number;
}

export interface TradeOrder {
  id: number;
  stock: Stock;
  type: string;
  status: string;
  price: number;
  initialQuantity: number;
  remainingQuantity: number;
  timestamp: string;
}

export interface MarketTick {
  symbol: string;
  price: number;
  timestamp: string;
  trend?: 'up' | 'down' | 'unchanged';
}

@Injectable({
  providedIn: 'root'
})
export class TradingService {
  private apiUrl = 'http://localhost:8080/api';
  private stompClient?: Client;
  private marketTickSubject = new Subject<MarketTick>();
  private tradeSubject = new Subject<any>();

  constructor(private http: HttpClient, @Inject(PLATFORM_ID) private platformId: Object) {
    if (isPlatformBrowser(this.platformId)) {
      this.stompClient = new Client({
        brokerURL: 'ws://localhost:8080/ws',
        debug: (str) => {
          console.log(str);
        },
        reconnectDelay: 5000,
        heartbeatIncoming: 4000,
        heartbeatOutgoing: 4000,
      });

      this.stompClient.onConnect = (frame) => {
        console.log('Connected: ' + frame);
        
        // Subscribe to all market data topics (AAP, GOOGL, TSLA)
        ['AAPL', 'GOOGL', 'TSLA'].forEach(symbol => {
          this.stompClient?.subscribe(`/topic/market/${symbol}`, (message: Message) => {
            if (message.body) {
              this.marketTickSubject.next(JSON.parse(message.body));
            }
          });
        });

        // Subscribe to user personal trades (Mocking userId 1 for Alice)
        this.stompClient?.subscribe(`/topic/trades/1`, (message: Message) => {
          if (message.body) {
             this.tradeSubject.next(JSON.parse(message.body));
          }
        });
      };

      this.stompClient.onStompError = (frame) => {
        console.error('Broker reported error: ' + frame.headers['message']);
        console.error('Additional details: ' + frame.body);
      };

      this.stompClient.activate();
    }
  }

  getMarketTicks(): Observable<MarketTick> {
    return this.marketTickSubject.asObservable();
  }

  getTradeUpdates(): Observable<any> {
    return this.tradeSubject.asObservable();
  }

  placeOrder(orderRequest: any): Observable<TradeOrder> {
    return this.http.post<TradeOrder>(`${this.apiUrl}/orders`, orderRequest);
  }

  getPortfolio(userId: number): Observable<Portfolio[]> {
    return this.http.get<Portfolio[]>(`${this.apiUrl}/portfolios/${userId}`);
  }

  getCashBalance(userId: number): Observable<number> {
    return this.http.get<number>(`${this.apiUrl}/portfolios/${userId}/cash`);
  }
}

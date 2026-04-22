import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';

@Component({
  selector: 'app-categories',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './categories.component.html',
  styleUrls: ['./categories.component.css']
})
export class CategoriesComponent {
  categories = [
    { id: 'nifty-50', name: 'Nifty 50', icon: '🇮🇳', description: 'Trade the top 50 Indian companies by market capitalization.' },
    { id: 'sensex', name: 'Sensex', icon: '📈', description: 'Invest in the top 30 established companies listed on the BSE.' },
    { id: 'stocks', name: 'US Stocks', icon: '🌎', description: 'Trade equities in real-time with our advanced matching engine.' },
    { id: 'mutual-funds', name: 'Mutual Funds', icon: '🏦', description: 'Invest in diversified portfolios managed by experts.' },
    { id: 'etfs', name: 'ETFs', icon: '🛒', description: 'Trade baskets of securities like a single stock.' },
    { id: 'options', name: 'Options', icon: '🎯', description: 'Leverage your trades with advanced derivative contracts.' },
    { id: 'futures', name: 'Futures', icon: '⏳', description: 'Speculate on the future price of various assets.' },
    { id: 'crypto', name: 'Crypto', icon: '₿', description: '24/7 trading for top cryptocurrencies globally.' }
  ];

  constructor(private router: Router) { }

  selectCategory(categoryId: string) {
    // For now, regardless of the category, we proceed to trade. 
    // In the future, this can be handled dynamically based on the category.
    this.router.navigate(['/trade'], { queryParams: { category: categoryId } });
  }
}

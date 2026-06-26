import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';

interface Feature {
  icon: string;
  title: string;
  description: string;
}

@Component({
  selector: 'app-features',
  standalone: true,
  imports: [CommonModule],
  template: `
    <section id="features" class="py-24 px-6 bg-[#0f1117]">
      <div class="max-w-6xl mx-auto">
        <div class="text-center mb-16">
          <h2 class="text-4xl font-bold text-[#e4e4e7] mb-4">Everything You Need to Own Your Hours</h2>
          <p class="text-[#9ca3af] text-lg max-w-2xl mx-auto">
            From raw CSV imports to detailed overtime analysis — CountMyHours turns your timesheets into actionable insights.
          </p>
        </div>
        <div class="grid md:grid-cols-2 lg:grid-cols-3 gap-6">
          @for (f of features; track f.title) {
            <div class="bg-[#1a1d27] border border-[#2a2d3a] rounded-xl p-6 hover:border-[#6366f1]/40 transition-all group">
              <div class="w-12 h-12 bg-[#6366f1]/10 rounded-xl flex items-center justify-center mb-4 group-hover:bg-[#6366f1]/20 transition-colors" [innerHTML]="f.icon"></div>
              <h3 class="text-[#e4e4e7] font-semibold text-lg mb-2">{{ f.title }}</h3>
              <p class="text-[#9ca3af] text-sm leading-relaxed">{{ f.description }}</p>
            </div>
          }
        </div>
      </div>
    </section>
  `
})
export class FeaturesComponent {
  features: Feature[] = [
    {
      icon: `<svg class="w-6 h-6 text-[#6366f1]" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M7 16a4 4 0 01-.88-7.903A5 5 0 1115.9 6L16 6a5 5 0 011 9.9M9 19l3 3m0 0l3-3m-3 3V10"/></svg>`,
      title: 'CSV & XLSX Import',
      description: 'Drop your timesheet in CSV or XLSX format and every row is instantly parsed, validated, and tagged by project. A built-in template makes it easy to get started.'
    },
    {
      icon: `<svg class="w-6 h-6 text-[#6366f1]" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z"/></svg>`,
      title: 'Dashboard & Charts',
      description: 'Stacked bar charts break down your yearly workload by project. Statistics cards show total hours, monthly average, gross extras, and net balance — filterable by year.'
    },
    {
      icon: `<svg class="w-6 h-6 text-[#6366f1]" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z"/></svg>`,
      title: 'Month Balance',
      description: 'See expected vs. worked hours for every month. Adjust vacation days and public holidays (with half-day support) — auto-filled from your country\'s calendar.'
    },
    {
      icon: `<svg class="w-6 h-6 text-[#6366f1]" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 7h8m0 0v8m0-8l-8 8-4-4-6 6"/></svg>`,
      title: 'Extras & Overtime',
      description: 'Annual overtime charts display gross extras, hours sold, and net balance per year. Per-project breakdowns show exactly where your extra hours came from.'
    },
    {
      icon: `<svg class="w-6 h-6 text-[#6366f1]" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8c-1.657 0-3 .895-3 2s1.343 2 3 2 3 .895 3 2-1.343 2-3 2m0-8c1.11 0 2.08.402 2.599 1M12 8V7m0 1v8m0 0v1m0-1c-1.11 0-2.08-.402-2.599-1M21 12a9 9 0 11-18 0 9 9 0 0118 0z"/></svg>`,
      title: 'Hour Selling',
      description: 'Log sold overtime monthly or yearly. Track vacation days sold and add notes. Sold hours are deducted from your gross extras so your net balance is always accurate.'
    },
    {
      icon: `<svg class="w-6 h-6 text-[#6366f1]" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 5h12M9 3v2m1.048 9.5A18.022 18.022 0 016.412 9m6.088 9h7M11 21l5-10 5 10M12.751 5C11.783 10.77 8.07 15.61 3 18.129"/></svg>`,
      title: '9 Languages',
      description: 'English, Português, Español, Italiano, 日本語, 中文, हिन्दी, and more. Switch language in Settings and the entire UI rebuilds instantly — including the right holiday calendar.'
    }
  ];
}

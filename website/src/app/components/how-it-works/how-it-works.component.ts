import { Component } from '@angular/core';

@Component({
  selector: 'app-how-it-works',
  standalone: true,
  template: `
    <section class="py-24 px-6 bg-[#0f1117]">
      <div class="max-w-6xl mx-auto">
        <div class="text-center mb-16">
          <h2 class="text-4xl font-bold text-[#e4e4e7] mb-4">How It Works</h2>
          <p class="text-[#9ca3af] text-lg">Get from raw timesheets to full insights in three steps.</p>
        </div>
        <div class="grid md:grid-cols-3 gap-8 relative">
          <!-- Connector line (desktop) -->
          <div class="hidden md:block absolute top-12 left-1/3 right-1/3 h-px bg-[#2a2d3a]"></div>

          @for (step of steps; track step.number) {
            <div class="text-center relative">
              <div class="w-24 h-24 bg-[#1a1d27] border-2 border-[#6366f1]/40 rounded-full flex items-center justify-center mx-auto mb-6 relative z-10">
                <span class="text-3xl font-bold text-[#6366f1]">{{ step.number }}</span>
              </div>
              <h3 class="text-[#e4e4e7] font-semibold text-xl mb-3">{{ step.title }}</h3>
              <p class="text-[#9ca3af] text-sm leading-relaxed">{{ step.description }}</p>
            </div>
          }
        </div>
      </div>
    </section>
  `
})
export class HowItWorksComponent {
  steps = [
    {
      number: '1',
      title: 'Export Your Timesheet',
      description: 'Use any spreadsheet tool — Numbers, Excel, or Google Sheets. Save your hours as a CSV file with semicolons. CountMyHours can generate a blank template for you to fill in.'
    },
    {
      number: '2',
      title: 'Import With One Click',
      description: 'Click Import and select your file. CountMyHours validates the format, parses each row, and stores everything locally on your Mac. No internet connection needed.'
    },
    {
      number: '3',
      title: 'Get Insights Instantly',
      description: 'Dashboard, charts, month balance, and overtime analysis update in real time. Track vacation, holidays, and sold hours. Your data never leaves your machine.'
    }
  ];
}

import { Component, signal } from '@angular/core';

@Component({
  selector: 'app-support',
  standalone: true,
  template: `
    <section id="support" class="py-24 px-6 bg-[#0f1117]">
      <div class="max-w-2xl mx-auto text-center">

        <div class="inline-flex items-center gap-2 bg-[#1a1d27] border border-[#2a2d3a] rounded-full px-4 py-1.5 mb-6">
          <svg class="w-4 h-4 text-[#6366f1]" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
              d="M18.364 5.636a9 9 0 11-12.728 0M12 3v9"/>
          </svg>
          <span class="text-[#9ca3af] text-sm">Support</span>
        </div>

        <h2 class="text-3xl md:text-4xl font-bold text-[#e4e4e7] mb-4">
          We're here to help
        </h2>
        <p class="text-[#9ca3af] text-lg mb-12">
          Have a question, found a bug, or want to suggest a feature?<br>
          Reach out — we read every message.
        </p>

        <div class="bg-[#1a1d27] border border-[#2a2d3a] rounded-2xl p-8 text-left">
          <div class="flex items-center gap-3 mb-6">
            <div class="w-10 h-10 bg-[#6366f1]/10 rounded-xl flex items-center justify-center">
              <svg class="w-5 h-5 text-[#6366f1]" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                  d="M3 8l7.89 5.26a2 2 0 002.22 0L21 8M5 19h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z"/>
              </svg>
            </div>
            <div>
              <p class="text-[#9ca3af] text-xs uppercase tracking-widest mb-0.5">Email</p>
              <p class="text-[#e4e4e7] font-semibold text-lg">countmyhour&#64;gmail.com</p>
            </div>
          </div>

          <div class="flex flex-col sm:flex-row gap-3">
            <a href="mailto:countmyhour@gmail.com"
               class="flex-1 inline-flex items-center justify-center gap-2 bg-[#6366f1] hover:bg-[#4f46e5] text-white font-semibold px-6 py-3 rounded-xl transition-colors">
              <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                  d="M3 8l7.89 5.26a2 2 0 002.22 0L21 8M5 19h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z"/>
              </svg>
              Send Email
            </a>
            <button (click)="copyEmail()"
               class="flex-1 inline-flex items-center justify-center gap-2 bg-[#2a2d3a] hover:bg-[#353848] text-[#e4e4e7] font-medium px-6 py-3 rounded-xl transition-colors">
              @if (copied()) {
                <svg class="w-4 h-4 text-green-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7"/>
                </svg>
                <span class="text-green-400">Copied!</span>
              } @else {
                <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                    d="M8 16H6a2 2 0 01-2-2V6a2 2 0 012-2h8a2 2 0 012 2v2m-6 12h8a2 2 0 002-2v-8a2 2 0 00-2-2h-8a2 2 0 00-2 2v8a2 2 0 002 2z"/>
                </svg>
                Copy Address
              }
            </button>
          </div>
        </div>

        <p class="text-[#9ca3af] text-sm mt-6">
          Typical response time: within 48 hours.
        </p>

      </div>
    </section>
  `
})
export class SupportComponent {
  copied = signal(false);

  copyEmail() {
    navigator.clipboard.writeText('countmyhour@gmail.com').then(() => {
      this.copied.set(true);
      setTimeout(() => this.copied.set(false), 2000);
    });
  }
}

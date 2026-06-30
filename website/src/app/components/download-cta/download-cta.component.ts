import { Component } from '@angular/core';

@Component({
  selector: 'app-download-cta',
  standalone: true,
  template: `
    <section id="download" class="py-24 px-6 bg-[#1a1d27]/40">
      <div class="max-w-5xl mx-auto">
        <div class="text-center mb-12 relative">
          <div class="absolute inset-0 pointer-events-none">
            <div class="absolute top-0 left-1/2 -translate-x-1/2 w-96 h-32 bg-[#6366f1]/8 blur-3xl rounded-full"></div>
          </div>
          <img src="assets/logo.png" alt="CountMyHours" class="w-16 h-16 mx-auto mb-5">
          <h2 class="text-4xl font-bold text-[#e4e4e7] mb-3">Get CountMyHours</h2>
          <p class="text-[#9ca3af] text-lg">One-time purchase · Yours forever · No subscription</p>
        </div>

        <!-- Platform cards -->
        <div class="grid md:grid-cols-2 gap-6 mb-10">

          <!-- macOS card -->
          <div class="bg-[#1a1d27] border border-[#2a2d3a] rounded-2xl p-8 flex flex-col items-center text-center hover:border-[#6366f1]/40 transition-all">
            <div class="w-14 h-14 bg-[#6366f1]/10 rounded-2xl flex items-center justify-center mb-5">
              <svg class="w-8 h-8 text-[#6366f1]" viewBox="0 0 24 24" fill="currentColor">
                <path d="M18.71 19.5c-.83 1.24-1.71 2.45-3.05 2.47-1.34.03-1.77-.79-3.29-.79-1.53 0-2 .77-3.27.82-1.31.05-2.3-1.32-3.14-2.53C4.25 17 2.94 12.45 4.7 9.39c.87-1.52 2.43-2.48 4.12-2.51 1.28-.02 2.5.87 3.29.87.78 0 2.26-1.07 3.8-.91.65.03 2.47.26 3.64 1.98-.09.06-2.17 1.28-2.15 3.81.03 3.02 2.65 4.03 2.68 4.04-.03.07-.42 1.44-1.38 2.83M13 3.5c.73-.83 1.94-1.46 2.94-1.5.13 1.17-.34 2.35-1.04 3.19-.69.85-1.83 1.51-2.95 1.42-.15-1.15.41-2.35 1.05-3.11z"/>
              </svg>
            </div>
            <h3 class="text-[#e4e4e7] font-bold text-xl mb-1">macOS</h3>
            <p class="text-[#9ca3af] text-sm mb-2">macOS 12 Monterey or later · v3.2</p>
            <div class="text-3xl font-bold text-[#e4e4e7] mb-1">$19.99</div>
            <p class="text-[#9ca3af] text-xs mb-6">one-time purchase</p>
            <a
              href="#"
              target="_blank"
              rel="noopener noreferrer"
              class="w-full inline-flex items-center justify-center gap-2 bg-[#6366f1] hover:bg-[#4f46e5] text-white font-semibold px-6 py-3.5 rounded-xl text-sm transition-all hover:shadow-lg hover:shadow-[#6366f1]/25">
              <!-- TODO: paste Gumroad macOS product URL in href above -->
              <svg class="w-5 h-5" viewBox="0 0 24 24" fill="currentColor">
                <path d="M18.71 19.5c-.83 1.24-1.71 2.45-3.05 2.47-1.34.03-1.77-.79-3.29-.79-1.53 0-2 .77-3.27.82-1.31.05-2.3-1.32-3.14-2.53C4.25 17 2.94 12.45 4.7 9.39c.87-1.52 2.43-2.48 4.12-2.51 1.28-.02 2.5.87 3.29.87.78 0 2.26-1.07 3.8-.91.65.03 2.47.26 3.64 1.98-.09.06-2.17 1.28-2.15 3.81.03 3.02 2.65 4.03 2.68 4.04-.03.07-.42 1.44-1.38 2.83M13 3.5c.73-.83 1.94-1.46 2.94-1.5.13 1.17-.34 2.35-1.04 3.19-.69.85-1.83 1.51-2.95 1.42-.15-1.15.41-2.35 1.05-3.11z"/>
              </svg>
              Buy for macOS
            </a>
            <p class="text-[#9ca3af] text-xs mt-3">Also available on the Mac App Store</p>
          </div>

          <!-- Windows card -->
          <div class="bg-[#1a1d27] border border-[#2a2d3a] rounded-2xl p-8 flex flex-col items-center text-center relative overflow-hidden">
            <div class="absolute top-4 right-4 bg-[#2a2d3a] text-[#9ca3af] text-xs font-medium px-3 py-1 rounded-full">
              Coming Soon
            </div>
            <div class="w-14 h-14 bg-[#2a2d3a] rounded-2xl flex items-center justify-center mb-5">
              <svg class="w-8 h-8 text-[#9ca3af]" viewBox="0 0 24 24" fill="currentColor">
                <path d="M0 3.449L9.75 2.1v9.451H0m10.949-9.602L24 0v11.4H10.949M0 12.6h9.75v9.451L0 20.699M10.949 12.6H24V24l-12.9-1.801"/>
              </svg>
            </div>
            <h3 class="text-[#e4e4e7] font-bold text-xl mb-1">Windows</h3>
            <p class="text-[#9ca3af] text-sm mb-2">Windows 10 / 11 · v3.2</p>
            <div class="text-3xl font-bold text-[#9ca3af] mb-1">$19.99</div>
            <p class="text-[#9ca3af] text-xs mb-6">one-time purchase</p>
            <a
              href="mailto:thimakgt@gmail.com?subject=CountMyHours%20Windows%20Waitlist&body=Hi%2C%20I'd%20like%20to%20be%20notified%20when%20CountMyHours%20for%20Windows%20is%20available."
              class="w-full inline-flex items-center justify-center gap-2 border border-[#2a2d3a] hover:border-[#6366f1]/50 text-[#9ca3af] hover:text-[#e4e4e7] font-semibold px-6 py-3.5 rounded-xl text-sm transition-all">
              <!-- TODO: replace href with Gumroad Windows product URL when .msi build is ready -->
              <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 17h5l-1.405-1.405A2.032 2.032 0 0118 14.158V11a6.002 6.002 0 00-4-5.659V5a2 2 0 10-4 0v.341C7.67 6.165 6 8.388 6 11v3.159c0 .538-.214 1.055-.595 1.436L4 17h5m6 0v1a3 3 0 11-6 0v-1m6 0H9"/>
              </svg>
              Notify Me When Ready
            </a>
            <p class="text-[#9ca3af] text-xs mt-3">We'll email you when Windows launches</p>
          </div>

        </div>

        <!-- Shared feature badges -->
        <div class="flex flex-wrap items-center justify-center gap-6 pt-8 border-t border-[#2a2d3a]">
          <div class="flex items-center gap-2 text-[#9ca3af] text-sm">
            <svg class="w-4 h-4 text-[#6366f1]" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z"/>
            </svg>
            100% Private
          </div>
          <div class="flex items-center gap-2 text-[#9ca3af] text-sm">
            <svg class="w-4 h-4 text-[#6366f1]" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m5.618-4.016A11.955 11.955 0 0112 2.944a11.955 11.955 0 01-8.618 3.04A12.02 12.02 0 003 9c0 5.591 3.824 10.29 9 11.622 5.176-1.332 9-6.03 9-11.622 0-1.042-.133-2.052-.382-3.016z"/>
            </svg>
            Your data never leaves your device
          </div>
          <div class="flex items-center gap-2 text-[#9ca3af] text-sm">
            <svg class="w-4 h-4 text-[#6366f1]" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 15a4 4 0 004 4h9a5 5 0 10-.1-9.999 5.002 5.002 0 10-9.78 2.096A4.001 4.001 0 003 15z"/>
            </svg>
            No Cloud
          </div>
          <div class="flex items-center gap-2 text-[#9ca3af] text-sm">
            <svg class="w-4 h-4 text-[#6366f1]" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 3v4M3 5h4M6 17v4m-2-2h4m5-16l2.286 6.857L21 12l-5.714 2.143L13 21l-2.286-6.857L5 12l5.714-2.143L13 3z"/>
            </svg>
            Pay once, keep forever
          </div>
        </div>
      </div>
    </section>
  `
})
export class DownloadCtaComponent {}

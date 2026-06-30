import { Component } from '@angular/core';

@Component({
  selector: 'app-footer',
  standalone: true,
  template: `
    <footer class="bg-[#0f1117] border-t border-[#2a2d3a] py-10 px-6">
      <div class="max-w-6xl mx-auto flex flex-col md:flex-row items-center justify-between gap-6">
        <div class="flex items-center gap-3">
          <img src="assets/logo.png" alt="CountMyHours" class="w-8 h-8">
          <span class="text-[#e4e4e7] font-medium">CountMyHours</span>
        </div>
        <p class="text-[#9ca3af] text-sm">
          © {{ year }} CountMyHours. Made with care for freelancers and professionals.
        </p>
        <div class="flex items-center gap-6">
          <a href="#features" class="text-[#9ca3af] hover:text-[#e4e4e7] text-sm transition-colors">Features</a>
          <a href="#screenshots" class="text-[#9ca3af] hover:text-[#e4e4e7] text-sm transition-colors">Screenshots</a>
          <a href="#support" class="text-[#9ca3af] hover:text-[#e4e4e7] text-sm transition-colors">Support</a>
          <a href="https://apps.apple.com/app/countmyhours" target="_blank" rel="noopener noreferrer" class="text-[#6366f1] hover:text-[#4f46e5] text-sm transition-colors">App Store</a>
        </div>
      </div>
    </footer>
  `
})
export class FooterComponent {
  year = new Date().getFullYear();
}

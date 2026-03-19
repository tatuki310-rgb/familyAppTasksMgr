from playwright.sync_api import sync_playwright

def run(playwright):
    browser = playwright.chromium.launch(headless=True)
    context = browser.new_context(
        record_video_dir="/home/jules/verification/video",
        viewport={"width": 375, "height": 812} # Simulate mobile view
    )
    page = context.new_page()

    page.goto("http://localhost:8080")
    page.wait_for_timeout(1000)

    # Take screenshot of the responsive index page
    page.screenshot(path="/home/jules/verification/verification.png", full_page=True)
    page.wait_for_timeout(1000)

    context.close()
    browser.close()

with sync_playwright() as playwright:
    run(playwright)

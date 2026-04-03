import "./fonts.css";
import "./globals.css";

export const metadata = {
  title: "Meet Assistant",
  description: "AI meeting summaries with live transcript",
};

export default function RootLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return (
    <html lang="en">
      <body>{children}</body>
    </html>
  );
}

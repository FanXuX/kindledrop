export async function send(data: { url: string; email: string }) {
  const response = await fetch("/api/send", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify({
      url: data.url,
      kindleEmail: data.email,
    }),
  });

  const result = await response.json();

  if (!response.ok) {
    throw new Error(
      result.message || `Request failed with status ${response.status}`
    );
  }

  return result;
}

export const apiClient = {
  send,
};

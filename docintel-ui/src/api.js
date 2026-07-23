const BASE_URL = "https://docintel-bzwh.onrender.com";

export async function uploadDocument(file) {
  const formData = new FormData();
  formData.append("file", file);

  const res = await fetch(`${BASE_URL}/documents/upload`, {
    method: "POST",
    body: formData,
  });
  if (!res.ok) throw new Error(`Upload failed: ${res.status}`);
  return res.json();
}

export async function processDocument(documentId) {
  const res = await fetch(`${BASE_URL}/documents/${documentId}/process`, {
    method: "POST",
  });
  if (!res.ok) throw new Error(`Processing failed: ${res.status}`);
  return res.json();
}

export async function getDocument(documentId) {
  const res = await fetch(`${BASE_URL}/documents/${documentId}`);
  if (!res.ok) throw new Error(`Fetch failed: ${res.status}`);
  return res.json();
}

export async function submitCorrections(documentId, corrections) {
  const res = await fetch(`${BASE_URL}/documents/${documentId}/corrections`, {
    method: "PUT",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ corrections }),
  });
  if (!res.ok) throw new Error(`Corrections failed: ${res.status}`);
  return res.json();
}
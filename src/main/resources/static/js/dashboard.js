async function fetchJson(url, options = {}) {
	const res = await fetch(url, options);
	const text = await res.text();
	if (!res.ok) throw new Error(text || "Error");
	try { return JSON.parse(text); } catch(e) { return text; }
}

function showMsg(msg, ok) {
	const el = document.getElementById('actionMessage');
	if (el) { el.textContent = msg; el.style.color = ok ? '#10b981' : '#f87171'; }
}

document.getElementById('btnDeposit').onclick = async () => {
	const amount = document.getElementById('depositAmount').value;
	const desc = document.getElementById('depositDesc').value;
	if(!amount || !desc) return showMsg("Amount and Description are mandatory", false);
	try {
		await fetchJson('/api/transactions/deposit', {
			method: 'POST',
			headers: { 'Content-Type': 'application/json' },
			body: JSON.stringify({ amount, description: desc })
		});
		location.reload();
	} catch(e) { showMsg(e.message, false); }
};

document.getElementById('btnWithdraw').onclick = async () => {
	const amount = document.getElementById('withdrawAmount').value;
	const desc = document.getElementById('withdrawDesc').value;
	if(!amount || !desc) return showMsg("Amount and Description are mandatory", false);
	try {
		await fetchJson('/api/transactions/withdraw', {
			method: 'POST',
			headers: { 'Content-Type': 'application/json' },
			body: JSON.stringify({ amount, description: desc })
		});
		location.reload();
	} catch(e) { showMsg(e.message, false); }
};

document.getElementById('btnTransfer').onclick = async () => {
	const email = document.getElementById('transferEmail').value;
	const amount = document.getElementById('transferAmount').value;
	const desc = document.getElementById('transferDesc').value;
	if(!email || !amount || !desc) return showMsg("Email, Amount and Note are mandatory", false);
	try {
		await fetchJson('/api/transactions/transfer/initiate', {
			method: 'POST',
			headers: { 'Content-Type': 'application/json' },
			body: JSON.stringify({ amount, receiverEmail: email, description: desc })
		});
		document.getElementById('otpOverlay').style.display = 'flex';
	} catch(e) { showMsg(e.message, false); }
};

document.getElementById('btnConfirmOtp').onclick = async () => {
	try {
		await fetchJson('/api/transactions/transfer/confirm', {
			method: 'POST',
			headers: { 'Content-Type': 'application/json' },
			body: JSON.stringify({ otpCode: document.getElementById('otpInput').value })
		});
		location.reload();
	} catch(e) { alert(e.message); }
};
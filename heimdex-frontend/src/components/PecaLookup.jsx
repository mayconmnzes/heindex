import React, { useState } from 'react';
import axios from 'axios';

const API_BASE = import.meta.env.VITE_API_BASE_URL;
const PECAS_LOOKUP = `${API_BASE}/api/pecas/lookup`;
const ESTOQUE_SAIDA_OS = `${API_BASE}/api/estoque/saida`; // POST /saida/{osId}
const ESTOQUE_SAIDA_AVULSA = `${API_BASE}/api/estoque/saida-avulsa`; // POST /saida-avulsa/{equipamentoId}

/**
 * Props:
 * - osId (optional)   : se informado, fará POST para /api/estoque/saida/{osId}
 * - equipamentoId (optional) : se informado e osId ausente, fará POST para /api/estoque/saida-avulsa/{equipamentoId}
 * - onSaved (optional) : callback() chamado quando consumo for salvo com sucesso
 */
export default function PecaLookup({ osId, equipamentoId, onSaved }) {
  const [code, setCode] = useState('');
  const [loading, setLoading] = useState(false);
  const [results, setResults] = useState(null); // null | [] | object
  const [selected, setSelected] = useState(null);
  const [quantidade, setQuantidade] = useState(1);
  const [error, setError] = useState(null);

  async function handleLookup(e) {
    e?.preventDefault();
    setError(null);
    const scannedCode = (code || '').trim();
    if (!scannedCode) {
      setError('Informe o código da peça.');
      return;
    }

    setLoading(true);
    try {
      const res = await axios.get(PECAS_LOOKUP, { params: { code: scannedCode } });
      const data = res.data;
      // backend pode retornar um objeto ou um array
      if (Array.isArray(data)) {
        setResults(data);
        setSelected(data.length === 1 ? data[0] : null);
      } else {
        // objeto -> consumo direto possível
        setResults(null);
        setSelected(data);
      }
    } catch (err) {
      setError(err.response?.data?.message || 'Peça não encontrada');
      setResults(null);
      setSelected(null);
    } finally {
      setLoading(false);
    }
  }

  async function handleConfirmConsumir() {
    setError(null);
    if (!selected || !selected.id) {
      setError('Selecione uma peça antes de confirmar.');
      return;
    }
    const dto = {
      pecaId: selected.id,
      quantidade: Number(quantidade) || 1
      // outros campos do DTO podem ser adicionados se necessário
    };

    try {
      if (osId) {
        await axios.post(`${ESTOQUE_SAIDA_OS}/${osId}`, dto);
      } else if (equipamentoId) {
        await axios.post(`${ESTOQUE_SAIDA_AVULSA}/${equipamentoId}`, dto);
      } else {
        throw new Error('Nem osId nem equipamentoId foram informados para realizar a saída.');
      }
      if (onSaved) onSaved();
      // limpa estado
      setCode('');
      setResults(null);
      setSelected(null);
      setQuantidade(1);
      setError(null);
      alert('Consumo registrado com sucesso.');
    } catch (err) {
      console.error(err);
      setError(err.response?.data || err.message || 'Erro ao registrar consumo');
    }
  }

  return (
    <div style={{ padding: 12, background: '#fff', borderRadius: 6 }}>
      <form onSubmit={handleLookup} style={{ display: 'flex', gap: 8, alignItems: 'center' }}>
        <input
          placeholder="Digite ou escaneie o código da peça"
          value={code}
          onChange={e => setCode(e.target.value)}
          style={{ flex: 1, padding: '8px 10px' }}
        />
        <button type="submit" disabled={loading} style={{ padding: '8px 12px' }}>
          {loading ? 'Buscando...' : 'Buscar'}
        </button>
      </form>

      {error && <div style={{ color: 'red', marginTop: 8 }}>{error}</div>}

      {/* Resultado único (objeto) ou seleção (array) */}
      {selected && (
        <div style={{ marginTop: 12, borderTop: '1px solid #eee', paddingTop: 12 }}>
          <div><strong>Peça selecionada:</strong> {selected.nome} (id: {selected.id})</div>
          <div style={{ marginTop: 8 }}>
            <label>Quantidade:&nbsp;
              <input type="number" min="1" value={quantidade} onChange={e => setQuantidade(e.target.value)} style={{ width: 80 }} />
            </label>
          </div>
          <div style={{ marginTop: 10 }}>
            <button onClick={handleConfirmConsumir} style={{ padding: '8px 12px' }}>Confirmar consumo</button>
          </div>
        </div>
      )}

      {results && Array.isArray(results) && results.length > 0 && (
        <div style={{ marginTop: 12 }}>
          <div><strong>Selecione a peça correta:</strong></div>
          <ul style={{ listStyle: 'none', paddingLeft: 0 }}>
            {results.map(r => (
              <li key={r.id} style={{ marginTop: 8 }}>
                <label style={{ cursor: 'pointer' }}>
                  <input
                    type="radio"
                    name="peca"
                    value={r.id}
                    checked={selected?.id === r.id}
                    onChange={() => setSelected(r)}
                  />{' '}
                  {r.nome} — codControle: {r.codigoControle || r.codigo_controle || '-'} — id: {r.id}
                </label>
              </li>
            ))}
          </ul>
          {selected && (
            <div style={{ marginTop: 10 }}>
              <label>Quantidade:&nbsp;
                <input type="number" min="1" value={quantidade} onChange={e => setQuantidade(e.target.value)} style={{ width: 80 }} />
              </label>
              <div style={{ marginTop: 8 }}>
                <button onClick={handleConfirmConsumir} style={{ padding: '8px 12px' }}>Confirmar consumo</button>
              </div>
            </div>
          )}
        </div>
      )}

      {results && Array.isArray(results) && results.length === 0 && (
        <div style={{ marginTop: 12, color: '#666' }}>Nenhuma peça encontrada.</div>
      )}
    </div>
  );
}
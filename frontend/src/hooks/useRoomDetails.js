import { useEffect, useState } from 'react';
import { roomService } from '../services/roomService';

export function useRoomDetails(roomId) {
  const [room, setRoom] = useState(null);
  const [images, setImages] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    let cancelled = false;
    async function load() {
      if (!roomId) return;
      setLoading(true);
      setError('');
      try {
        const [roomData, imageData] = await Promise.all([
          roomService.getById(roomId),
          roomService.getImages(roomId).catch(() => []),
        ]);
        if (!cancelled) {
          setRoom(roomData);
          setImages(imageData?.length ? imageData : roomData.images || []);
        }
      } catch (err) {
        if (!cancelled) {
          setError(err.message || 'Room not found');
          setRoom(null);
        }
      } finally {
        if (!cancelled) setLoading(false);
      }
    }
    load();
    return () => {
      cancelled = true;
    };
  }, [roomId]);

  return { room, images, loading, error };
}
